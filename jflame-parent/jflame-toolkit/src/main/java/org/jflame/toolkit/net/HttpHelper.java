package org.jflame.toolkit.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jflame.toolkit.exception.RemoteAccessException;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.MapHelper;
import org.jflame.toolkit.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http请求工具类,支持接收和提交cookie
 * <p>
 * 默认属性:charset=utf-8, connectionTimeout=1500 * 60, readTimeout=1000 * 60
 * <p>
 * 如果不修改默认请求属性可直接使用便捷的静态方法: <br>
 * HttpHelper.sendGet(url)<br>
 * HttpHelper.sendPost(url,param)
 * <p>
 * 实例化方式使用本类,可以修改默认属性,或者直接获取内部HttpURLConnection设置.示例:<br />
 * <code><pre>
 *  HttpHelper helper=new HttpHelper();
 *  helper.setCharset("gbk");
 *  helper.getConnection("http://www.qq.com",HttpMethod.GET,null);
 *  String resultText=helper.sendRequest(null);</pre>
 * </code>
 * 
 * @author zyc
 */
public final class HttpHelper {

    private final Logger log = LoggerFactory.getLogger(HttpHelper.class);

    /**
     * HTTP请求方式枚举
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    private final String accept = "text/html,application/json,application/xhtml+xml, */*";
    private final String contentTypePost = "application/x-www-form-urlencoded";
    private final String userAgent = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";

    private int connectionTimeout = 1500 * 60;
    private int readTimeout = 1000 * 60;
    private URL requestUrl;
    private HttpURLConnection conn;
    private String charset;
    private HttpMethod method;
    private Map<String,String> headers;
    // cookie管理器
    private CookieManager cookieManager;

    /**
     * 构造函数.使用默认cookie管理策略
     */
    public HttpHelper() {
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    public HttpURLConnection getConnection(String url, HttpMethod method) throws RemoteAccessException {
        return getConnection(url, method, null);
    }

    /**
     * 创建一个http连接,注:此连接未打开,可设置属性
     * 
     * @param url 请求地址
     * @param method 请求方法
     * @param httpHeaders http头部属性
     * @throws RemoteAccessException
     */
    public HttpURLConnection getConnection(String url, HttpMethod method, Map<String,String> httpHeaders)
            throws RemoteAccessException {
        headers = httpHeaders;
        try {
            requestUrl = new URL(url);
            this.method = method;
            conn = (HttpURLConnection) requestUrl.openConnection();
            if (conn instanceof HttpsURLConnection) {
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) conn).setHostnameVerifier(trustAnyHost);
            }

            conn.setRequestMethod(this.method.name());
            // 设置通用属性
            conn.setConnectTimeout(getConnectionTimeout());
            conn.setReadTimeout(getReadTimeout());
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("Accept", accept);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setUseCaches(false);
            HttpURLConnection.setFollowRedirects(true);
            // 设置cookie
            String cookies = getCookies(requestUrl.toURI());
            if (StringHelper.isNotEmpty(cookies)) {
                conn.setRequestProperty("Cookie", cookies);
            }
            if (method == HttpMethod.POST) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", contentTypePost);
            }

            if (headers != null && !headers.isEmpty()) {
                for (Entry<String,String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException e) {
            throw new RemoteAccessException("获取http连接异常,url:" + url, e);
        } catch (URISyntaxException e) {
            throw new RemoteAccessException(requestUrl + "转URI失败", e);
        }
        return conn;
    }

    /**
     * 发起请求,并返回结果文本
     * 
     * @param params 请求参数,参数为表单类型
     * @return 返回结果文本,没有结果或返回http状态码不是200将返回null
     * @throws RemoteAccessException
     */
    public String sendRequest(Map<String,String> params) throws RemoteAccessException {
        try {
            conn.connect();
            if (!MapHelper.isEmpty(params)) {
                // 输出参数
                try (OutputStream outStream = conn.getOutputStream();
                        OutputStreamWriter outWriter = new OutputStreamWriter(outStream,
                                StringHelper.isEmpty(charset) ? CharsetHelper.UTF_8 : charset)) {
                    outWriter.write(StringHelper.buildUrlParamFromMap(params));
                    outWriter.flush();
                }
            }
            log.debug("发起http请求:url={},方式={}", requestUrl, this.method);
            return getResponseText();
        } catch (IOException e) {
            throw new RemoteAccessException("http请求异常,url:" + requestUrl, e);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * 发起请求,参数是byte数组,发送非表单类型参数请使用该方法
     * 
     * @param params 请求参数
     * @return
     * @throws RemoteAccessException
     */
    public String sendRequest(byte[] params) throws RemoteAccessException {
        try {
            if (this.method == HttpMethod.GET) {
                conn.setRequestMethod(HttpMethod.POST.name());
            }
            conn.connect();
            if (params != null && params.length > 0) {
                // 输出参数
                try (OutputStream outStream = conn.getOutputStream();) {
                    outStream.write(params);
                    outStream.flush();
                }
            }
            log.debug("发起http请求:url={},方式={}", requestUrl, this.method);
            return getResponseText();
        } catch (IOException e) {
            throw new RemoteAccessException("http请求异常,url:" + requestUrl, e);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * 接收返回文本结果，并转为指定字符集的文本
     * 
     * @return
     * @throws IOException i/o error
     * @throws RemoteAccessException 请求返回状态码>300视为请求异常
     */
    private String getResponseText() throws RemoteAccessException, IOException {
        int statusCode = conn.getResponseCode();
        log.debug("请求结果:url:{},status={}",requestUrl, statusCode);
        if (statusCode < 300) {
            String respCharset = detectCharset(conn.getContentType());
            try (InputStream inStream = getInputStream(conn)) {
                return IOHelper.readString(inStream,
                        StringHelper.isEmpty(respCharset) ? CharsetHelper.UTF_8 : respCharset);
            }
        } else {
            throw new RemoteAccessException("请求失败" + requestUrl, statusCode);
        }
    }

    private byte[] getResponseBytes() throws IOException,RemoteAccessException {
        int statusCode = conn.getResponseCode();
        log.debug("请求结果:url:{},status={}",requestUrl, statusCode);
        if (statusCode < 300) {
            try (InputStream inStream = getInputStream(conn)) {
                return IOHelper.readBytes(inStream);
            }
        } else {
            throw new RemoteAccessException("请求失败" + requestUrl,statusCode);
        }
    }

    private InputStream getInputStream(HttpURLConnection conn) throws IOException {
        String contentEncoding = conn.getHeaderField("Content-Encoding");
        if (contentEncoding != null) {
            contentEncoding = contentEncoding.toLowerCase();
            if (contentEncoding.indexOf("gzip") != 1) {
                return new GZIPInputStream(conn.getInputStream());
            } else if (contentEncoding.indexOf("deflate") != 1) {
                return new DeflaterInputStream(conn.getInputStream());
            }
        }
        return conn.getInputStream();
    }

    private final String charsetRegex = "charset=\"?([\\w\\d-]+)\"?;?";

    private String detectCharset(String input) {
        if (StringHelper.isEmpty(input)) {
            Pattern pattern = Pattern.compile(charsetRegex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * 执行一个http get请求,使用默认属性
     * 
     * @param url 请求地址
     * @return
     * @throws RemoteAccessException
     */
    public static String sendGet(String url) throws RemoteAccessException {
        HttpHelper helper = new HttpHelper();
        helper.getConnection(url, HttpMethod.GET, null);
        return helper.sendRequest((Map<String,String>) null);
    }

    /**
     * 执行一个post请求,使用默认属性
     * 
     * @param url 请求地址
     * @param params 请求参数
     * @return
     * @throws IOException
     */
    public static String sendPost(String url, Map<String,String> params) throws RemoteAccessException {
        HttpHelper helper = new HttpHelper();
        helper.getConnection(url, HttpMethod.POST, null);
        return helper.sendRequest(params);
    }

    /**
     * https 域名校验
     */
    private class TrustAnyHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * https 证书管理
     */
    private class TrustAnyTrustManager implements X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }

    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
    private static final TrustAnyHostnameVerifier trustAnyHost = new HttpHelper().new TrustAnyHostnameVerifier();

    private static SSLSocketFactory initSSLSocketFactory() {
        try {
            TrustManager[] tm = { new HttpHelper().new TrustAnyTrustManager() };
            SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addCookie(String name, String value) throws URISyntaxException {
        HttpCookie cookie = new HttpCookie(name, value);
        cookieManager.getCookieStore().add(requestUrl.toURI(), cookie);
    }

    String getCookies(URI uri) {
        List<HttpCookie> cookies = cookieManager.getCookieStore().get(uri);
        if (cookies != null) {
            StringBuilder strBuf = new StringBuilder();
            for (HttpCookie httpCookie : cookies) {
                strBuf.append(';').append(httpCookie.getName()).append('=').append(httpCookie.getValue());
            }
            if (strBuf.length() > 1) {
                strBuf.deleteCharAt(0);
                return strBuf.toString();
            }
        }
        return null;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * 设置连接超时,单位毫秒
     * 
     * @param connectionTimeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * 设置读取超时,单位毫秒
     * 
     * @param readTimeout
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getCharset() {
        return charset;
    }

    /**
     * 设置请求编码,默认使用utf-8
     * 
     * @throws IllegalArgumentException 不支持字符集
     */
    public void setCharset(String charset) {
        if (CharsetHelper.isSupported(charset)) {
            this.charset = charset;
        } else {
            throw new IllegalArgumentException("不支持的字符集" + charset);
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public Map<String,String> getHeaders() {
        return headers;
    }

    /**
     * 设置http请求头
     * 
     * @param headers
     */
    public void setHeaders(Map<String,String> headers) {
        this.headers = headers;
    }
}
