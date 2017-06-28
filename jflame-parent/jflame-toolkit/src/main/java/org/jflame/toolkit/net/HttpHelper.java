package org.jflame.toolkit.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
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

import org.apache.commons.lang3.ArrayUtils;
import org.jflame.toolkit.codec.TranscodeException;
import org.jflame.toolkit.common.bean.CallResult.ResultEnum;
import org.jflame.toolkit.common.bean.pair.KeyValuePair;
import org.jflame.toolkit.common.bean.pair.NameValuePair;
import org.jflame.toolkit.exception.RemoteAccessException;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.JsonHelper;
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
 * HttpHelper.get(url)<br>
 * HttpHelper.post(url,param)
 * <p>
 * 示例:<br>
 * 
 * <pre>
 * <code>
 *  HttpHelper helper=new HttpHelper();
 *  helper.setCharset("gbk");
 *  boolean inited=helper.initConnect("http://www.qq.com",HttpMethod.POST);
 *  List&lt;NameValuePair&gt; params=new ArrayList&lt;&gt;();
 *  params.add(new NameValuePair("paramName","paramValue"));
 *  if(inited){
 *      HttpResponse result=helper.sendRequest(params);
 *      if(result.success()){
 *          System.out.print(result.getData());
 *      }
 *  }
 * </code>
 * </pre>
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
     * 构造函数.使用默认cookie管理策略.
     */
    public HttpHelper() {
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    /**
     * 初始一个http连接
     * 
     * @param url 请求地址
     * @param method 请求方法.HttpMethod
     * @return true初始成功,false失败
     */
    public boolean initConnect(String url, HttpMethod method) {
        return initConnect(url, method, null);
    }

    /**
     * 初始一个http连接,设置请求头,cookie,请求方式等
     * 
     * @param url 请求地址
     * @param method 请求方法.HttpMethod
     * @param httpHeaders http请求头
     * @return true初始成功,false失败
     */
    public boolean initConnect(String url, HttpMethod method, Map<String,String> httpHeaders) {
        headers = httpHeaders;
        boolean isConnected = false;

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
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestProperty("Accept", accept);
            conn.setRequestProperty("Charset", getCharset());
            conn.setConnectTimeout(getConnectionTimeout());
            conn.setReadTimeout(getReadTimeout());
            HttpURLConnection.setFollowRedirects(true);
            conn.setUseCaches(false);
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
            isConnected = true;
        } catch (IOException | URISyntaxException e) {
            isConnected = false;
            if (conn != null) {
                conn = null;
            }
            log.error("建立http连接失败,url:" + url, e);
        }

        return isConnected;
    }

    /**
     * 发送http请求
     * 
     * @param params byte[]请求参数,get方式时必须为Null
     * @param responseHandler 结果处理回调接口
     * @param <T> 最终返回的结果类型{@link IResponseResultHandler}
     * @return T
     * @throws RemoteAccessException
     */
    public <T> T sendRequest(byte[] params, IResponseResultHandler<T> responseHandler) throws RemoteAccessException {
        OutputStream outStream = null;
        try {
            conn.connect();
            if (params != null && params.length > 0) {
                // 提交参数
                outStream = conn.getOutputStream();
                outStream.write(params);
                outStream.flush();
            }
            return responseHandler.handle(conn);
        } catch (SocketTimeoutException e) {
            throw new RemoteAccessException("请求超时,url:" + requestUrl, HttpURLConnection.HTTP_CLIENT_TIMEOUT, e);
        } catch (Exception e) {
            throw new RemoteAccessException("http请求异常,url:" + requestUrl, e);
        } finally {
            IOHelper.closeQuietly(outStream);
            conn.disconnect();
        }
    }

    /**
     * 发起请求,并返回结果文本
     * 
     * @param params 请求参数
     * @return 返回结果文本,没有结果或返回http状态码不是200将返回null
     */
    public HttpResponse sendRequest(List<NameValuePair> params) {
        byte[] paramBytes = null;
        HttpResponse response = null;
        log.debug("发起http请求:url={},方式={},参数={}", requestUrl, this.method, ArrayUtils.toString(params));
        try {
            if (CollectionHelper.isNotEmpty(params)) {
                paramBytes = StringHelper.getBytes(KeyValuePair.toUrlParam(params), getCharset());
            }
            response = sendRequest(paramBytes, new DefalutResponse(0));
        } catch (TranscodeException e) {
            response = new HttpResponse(ResultEnum.PARAM_ERROR.getStatus(), "编码错误:" + getCharset());
            log.error("", e);
        } catch (RemoteAccessException e) {
            if (response == null) {
                response = new HttpResponse();
            }
            response.setStatus(e.getStatusCode() > 0 ? e.getStatusCode() : ResultEnum.SERVER_ERROR.getStatus());
            response.setMessage(e.getMessage());
            log.error("", e);
        }
        return response;
    }

    /**
     * 发起请求,http body参数
     * 
     * @param bodyParam http body参数字符串
     * @return
     */
    public HttpResponse sendRequest(String bodyParam) {
        byte[] paramBytes = null;
        HttpResponse response = null;
        log.debug("发起http请求:url={},方式={},body参数={}", requestUrl, "post", this.method, bodyParam);
        try {
            this.method = HttpMethod.POST;
            if (StringHelper.isNotEmpty(bodyParam)) {
                paramBytes = StringHelper.getBytes(bodyParam, getCharset());
            }
            response = sendRequest(paramBytes, new DefalutResponse(0));
        } catch (TranscodeException e) {
            response = new HttpResponse(ResultEnum.PARAM_ERROR.getStatus(), "编码错误:" + getCharset());
            log.error("", e);
        } catch (RemoteAccessException e) {
            if (response == null) {
                response = new HttpResponse();
            }
            response.setStatus(e.getStatusCode() > 0 ? e.getStatusCode() : ResultEnum.SERVER_ERROR.getStatus());
            response.setMessage(e.getMessage());
            log.error("", e);
        }
        return response;
    }

    /**
     * 发起请求,参数是byte[],注：请求必须是POST方式
     * 
     * @param params byte[]请求参数
     * @return 返回byte[]内容,未编码
     */
    public HttpResponse sendRequest(byte[] params) {
        HttpResponse response = null;
        log.debug("发起http请求:url={},方式={},参数byte[]", requestUrl, this.method);
        try {
            response = sendRequest(params, new DefalutResponse(1));
        } catch (RemoteAccessException e) {
            if (response == null) {
                response = new HttpResponse();
            }
            response.setStatus(e.getStatusCode() > 0 ? e.getStatusCode() : ResultEnum.SERVER_ERROR.getStatus());
            response.setMessage(e.getMessage());
            log.error("", e);
        }
        return response;
    }

    // 含参数和文件上传表单报文示例：
    // -------***anysplit
    // Content-Disposition: form-data; name="username"
    //
    // hello word
    // -------***anysplit
    // Content-Disposition: form-data; name="file1"; filename="D:/haha.txt"
    // Content-Type: text/plain
    //
    // filebytes
    // filebytes
    // -------***anysplit
    // Content-Disposition: form-data; name="file2"; filename="D:/huhu.txt"
    // Content-Type: text/plain
    //
    // filebytes
    // filebytes
    // -------***anysplit--
    /**
     * 发送上传文件请求.混合表单普通域和文件域
     * 
     * @param params 普通参数
     * @param uploadFiles 上传文件域,Map的key为文件域name
     * @return
     */
    public HttpResponse sendRequest(List<NameValuePair> params, Map<String,File> uploadFiles) {
        HttpResponse response = null;
        String boundary = "-----***anysplit";
        String newLine = "\r\n";
        String prefix = "--";
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        StringBuilder paramStrBuf = new StringBuilder(50);
        DataOutputStream outStream = null;
        log.debug("发起http请求:url={}", requestUrl);
        try {
            if (method != HttpMethod.POST) {
                conn.setRequestMethod(HttpMethod.POST.name());
                conn.setDoOutput(true);
            }
            conn.connect();
            outStream = new DataOutputStream(conn.getOutputStream());
            // 提交普通参数
            if (CollectionHelper.isNotEmpty(params)) {
                for (NameValuePair paramKv : params) {
                    if (StringHelper.isNotEmpty(paramKv.getKey())) {
                        paramStrBuf.append(prefix).append(boundary).append(newLine);
                        paramStrBuf.append("Content-Disposition: form-data; name=\"").append(paramKv.getKey())
                                .append("\"").append(newLine);
                        paramStrBuf.append(newLine);
                        paramStrBuf.append(paramKv.getValue()).append(newLine);
                    }
                }
                IOHelper.writeText(paramStrBuf.toString(), outStream, getCharset());
            }
            // 提交文件域参数，上传文件
            if (!MapHelper.isEmpty(uploadFiles)) {
                for (Map.Entry<String,File> fileParam : uploadFiles.entrySet()) {
                    if (StringHelper.isEmpty(fileParam.getKey())) {
                        throw new IllegalArgumentException("表单文件域名称不能为空", null);
                    }
                    if (!fileParam.getValue().exists()) {
                        throw new FileNotFoundException("文件不存在:" + fileParam.getValue().getName());
                    }
                    paramStrBuf.setLength(0);
                    paramStrBuf.append(prefix).append(boundary).append(newLine);
                    paramStrBuf.append("Content-Disposition: form-data; name=\"").append(fileParam.getKey())
                            .append("\";");
                    paramStrBuf.append("filename=\"").append(fileParam.getValue().getName()).append("\"")
                            .append(newLine);
                    paramStrBuf.append("Content-Type: application/octet-stream; charset=").append(getCharset())
                            .append(newLine);
                    paramStrBuf.append(newLine);
                    IOHelper.writeText(paramStrBuf.toString(), outStream, getCharset());
                    try (InputStream fileStream = Files.newInputStream(fileParam.getValue().toPath())) {
                        IOHelper.copy(fileStream, outStream);
                    } catch (IOException e) {
                        throw e;
                    }
                }
            }
            // 请求结束标记--boundary--\r\n
            IOHelper.writeText((prefix + boundary + prefix + newLine), outStream, getCharset());
            outStream.flush();
            // 处理返回结果,实际数据作为文本
            DefalutResponse responseHandler = new DefalutResponse(0);
            response = responseHandler.handle(conn);
        } catch (SocketTimeoutException e) {
            response = new HttpResponse(HttpURLConnection.HTTP_CLIENT_TIMEOUT, "请求超时");
        } catch (Exception e) {
            response = new HttpResponse(ResultEnum.SERVER_ERROR.getStatus(), e.getMessage());
            log.error("", e);
        } finally {
            IOHelper.closeQuietly(outStream);
            conn.disconnect();
        }
        return response;
    }

    /**
     * 缺省请求结果处理
     */
    class DefalutResponse implements IResponseResultHandler<HttpResponse> {

        private int resultType = 0;

        public DefalutResponse() {

        }

        public DefalutResponse(int resultType) {
            this.resultType = resultType;
        }

        @Override
        public HttpResponse handle(HttpURLConnection httpConn) throws IOException {
            HttpResponse result = new HttpResponse();
            result.setStatus(httpConn.getResponseCode());
            log.debug("请求结果:url:{},status={}", requestUrl, result.getStatus());
            result.setHeaders(httpConn.getHeaderFields());
            String respCharset = detectCharset(httpConn.getContentType());
            if (result.success()) {
                try (InputStream inStream = getInputStream(httpConn)) {
                    if (resultType == 0) {
                        result.setData(IOHelper.readText(inStream,
                                StringHelper.isEmpty(respCharset) ? CharsetHelper.UTF_8 : respCharset));

                    } else if (resultType == 1) {
                        result.setData(IOHelper.readBytes(inStream));
                    }
                } catch (IOException e) {
                    throw e;
                }
            } else {
                if (null != httpConn.getErrorStream()) {
                    try (InputStream inStream = httpConn.getErrorStream()) {
                        result.setMessage(IOHelper.readText(inStream,
                                StringHelper.isEmpty(respCharset) ? CharsetHelper.UTF_8 : respCharset));
                    } catch (IOException e) {
                        throw e;
                    }
                } else {
                    result.setData(httpConn.getResponseMessage());
                }
                log.error("请求失败," + result);
            }
            return result;
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
        if (StringHelper.isNotEmpty(input)) {
            Pattern pattern = Pattern.compile(charsetRegex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * 执行一个get请求，使用默认属性.
     * 
     * @param url 请求地址
     * @return HttpResponse请求结果,实际数据为文本字符
     */
    public static HttpResponse get(String url) {
        HttpHelper helper = new HttpHelper();
        boolean inited = helper.initConnect(url, HttpMethod.GET, null);
        if (inited) {
            return helper.sendRequest((List<NameValuePair>) null);
        } else {
            return new HttpResponse(HttpURLConnection.HTTP_UNAVAILABLE, "建立连接失败");
        }
    }

    /**
     * 执行一个post请求,使用默认属性
     * 
     * @param url 请求地址
     * @param params 请求参数
     * @return HttpResponse请求结果,实际数据为文本字符
     */
    public static HttpResponse post(String url, List<NameValuePair> params) {
        HttpHelper helper = new HttpHelper();
        boolean inited = helper.initConnect(url, HttpMethod.POST, null);
        if (inited) {
            return helper.sendRequest(params);
        } else {
            return new HttpResponse(HttpURLConnection.HTTP_UNAVAILABLE, "建立连接失败");
        }
    }

    /**
     * 执行一个post请求,使用默认属性,提交json内容
     * 
     * @param url 请求地址
     * @param entity 提交对象
     * @return
     */
    public static <T> HttpResponse postJson(String url, T entity) {
        HttpHelper helper = new HttpHelper();
        boolean inited = helper.initConnect(url, HttpMethod.POST, null);
        if (inited) {
            return helper.sendRequest(JsonHelper.toJson(entity));
        } else {
            return new HttpResponse(HttpURLConnection.HTTP_UNAVAILABLE, "建立连接失败");
        }
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

    /**
     * 设置cookie
     * 
     * @param cookieName cookie name
     * @param cookieValue cookie value
     */
    public void addCookie(String cookieName, String cookieValue) {
        if (cookieName != null) {
            HttpCookie cookie = new HttpCookie(cookieName, cookieValue);
            try {
                cookieManager.getCookieStore().add(requestUrl.toURI(), cookie);
            } catch (URISyntaxException e) {
                throw new RuntimeException("设置cookie失败,url不正确", e);
            }
        }
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
        return charset == null ? CharsetHelper.UTF_8 : charset;
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
