package org.jflame.toolkit.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
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

import org.jflame.toolkit.common.bean.CallResult.ResultEnum;
import org.jflame.toolkit.common.bean.pair.KeyValuePair;
import org.jflame.toolkit.common.bean.pair.NameValuePair;
import org.jflame.toolkit.net.http.HttpResponse;
import org.jflame.toolkit.net.http.RequestProperty;
import org.jflame.toolkit.net.http.handler.JsonRequestBodyHandler;
import org.jflame.toolkit.net.http.handler.RequestBodyHandler;
import org.jflame.toolkit.net.http.handler.TextRequestBodyHandler;
import org.jflame.toolkit.net.http.handler.XmlRequestBodyHandler;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
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

    public final static String accept = "text/html,application/json,application/xhtml+xml, */*";
    public final static String contentTypePost = "application/x-www-form-urlencoded";
    public final static String userAgent = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)";
    public final static String headContentType = "Content-Type";
    private final static int defaultConnTimeout = 1500 * 60;
    private final static int defaultReadTimeout = 1000 * 60;
    private URL requestUrl;
    private HttpURLConnection conn;
    private RequestProperty requestProperty;
    // cookie管理器
    private CookieManager cookieManager;
    private SSLSocketFactory sslSocketFactory;
    private TrustAnyHostnameVerifier trustVerifier;

    /**
     * 构造函数.设置缺省请求属性,默认编码为utf-8
     */
    public HttpHelper() {
        requestProperty = new RequestProperty(defaultConnTimeout, defaultReadTimeout, CharsetHelper.UTF_8);
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    /**
     * 使用默认属性初始一个连接
     * 
     * @param url 连接地址
     * @return
     */
    public boolean initConnect(String url) {
        boolean isOpened = false;
        try {
            requestUrl = new URL(url);
            conn = (HttpURLConnection) requestUrl.openConnection();
            if (conn instanceof HttpsURLConnection) {
                if (trustVerifier == null) {
                    trustVerifier = new HttpHelper().new TrustAnyHostnameVerifier();
                }
                if (sslSocketFactory == null) {
                    sslSocketFactory = initDefaultSSLSocketFactory();
                }
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) conn).setHostnameVerifier(trustVerifier);
            }
            if (getMethod() == null) {
                setMethod(HttpMethod.POST);
            }
            if (getCharset() == null) {
                setCharset(CharsetHelper.UTF_8);
            }
            isOpened = true;
        } catch (IOException e) {
            isOpened = false;
            if (conn != null) {
                conn = null;
            }
            log.error("建立http连接失败,url:" + url, e);
        }

        return isOpened;
    }

    /**
     * 使用指定请求属性初始一个连接
     * 
     * @param url 请求地址
     * @param requestProperty 请求属性
     * @return
     */
    public boolean initConnect(String url, RequestProperty requestProperty) {
        this.requestProperty = requestProperty;
        return initConnect(url);
    }

    /**
     * 初始一个http连接
     * 
     * @param url 请求地址
     * @param method 请求方法.HttpMethod
     * @return true初始成功,false失败
     */
    public boolean initConnect(String url, HttpMethod method) {
        setMethod(method);
        return initConnect(url);
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
        setMethod(method);
        setHeaders(httpHeaders);
        return initConnect(url);
    }

    /**
     * 发起请求,参数使用body发送
     * 
     * @param requestData 请求参数
     * @param requestBodyHandler 请求参数处理器
     * @return
     */
    public <T> HttpResponse sendRequest(T requestData, RequestBodyHandler<T> requestBodyHandler) {
        OutputStream outStream = null;
        HttpResponse response = new HttpResponse();
        try {
            setConnectionProperty();
            conn.connect();
            // post请求时提交参数
            if (getMethod() == HttpMethod.POST && requestData != null) {
                byte[] params = requestBodyHandler.handle(requestData, requestProperty);
                if (params != null && params.length > 0) {
                    outStream = conn.getOutputStream();
                    outStream.write(params);
                    outStream.flush();
                }
            }
            response = getResponse(conn);
        } catch (Exception e) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            response.setMessage(e.getMessage());
            log.error("http请求异常,url:" + requestUrl, e);
        } finally {
            IOHelper.closeQuietly(outStream);
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
    }

    /**
     * 发起请求,无请求参数
     * 
     * @return HttpResponse
     */
    public HttpResponse sendRequest() {
        return sendRequest((String) null);
    }

    /**
     * 发起请求
     * 
     * @param params 请求参数 List&lt;NameValuePair&gt;
     * @return HttpResponse
     */
    public HttpResponse sendRequest(List<NameValuePair> params) {
        return sendRequest(KeyValuePair.toUrlParam(params));
    }

    /**
     * 发起请求,http body字符串参数
     * 
     * @param bodyParam http body参数字符串
     * @return HttpResponse
     */
    public HttpResponse sendRequest(String bodyParam) {
        log.debug("发起http请求:url={},方式={},body参数={}", requestUrl, getMethod(), bodyParam);
        return sendRequest(bodyParam, new TextRequestBodyHandler());
    }

    /**
     * 发起请求,参数是byte[],注：请求必须是POST方式
     * 
     * @param params byte[]请求参数
     * @return HttpResponse
     */
    public HttpResponse sendRequest(byte[] params) {
        log.debug("发起http请求:url={},方式={},参数byte[]", requestUrl, getMethod());
        return sendRequest(params, new ByteRequestBodyHandler());
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
            if (getMethod() != HttpMethod.POST) {
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
            response = getResponse(conn);
        } catch (Exception e) {
            response = new HttpResponse(ResultEnum.SERVER_ERROR.getStatus(), e.getMessage());
            log.error("http文件域请求异常,url:" + requestUrl, e);
        } finally {
            IOHelper.closeQuietly(outStream);
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
    }

    /**
     * 发送请求,将java对象转为JSON字符串提交
     * 
     * @param entity 要提交的java对象
     * @return
     */
    public <T extends Serializable> HttpResponse sendJsonRequest(T entity) {
        addHeader(headContentType, "application/json;charset=" + getCharset());
        return sendRequest(entity, new JsonRequestBodyHandler());
    }

    /**
     * 发送请求,将java对象转为xml字符串提交
     * 
     * @param entity 要提交的java对象
     * @return
     */
    public <T> HttpResponse sendXmlRequest(T entity) {
        conn.setRequestProperty(headContentType, "application/xml");
        return sendRequest(entity, new XmlRequestBodyHandler());
    }

    /**
     * 处理请求结果
     * 
     * @param httpConn 连接
     * @return HttpResponse
     * @throws IOException
     */
    private HttpResponse getResponse(HttpURLConnection httpConn) throws IOException {
        HttpResponse result = new HttpResponse();
        result.setStatus(httpConn.getResponseCode());
        log.debug("请求结果:url:{},status={}", requestUrl, result.getStatus());
        result.setHeaders(httpConn.getHeaderFields());
        String respCharset = detectCharset(httpConn.getContentType());
        if (StringHelper.isNotEmpty(respCharset)) {
            result.setCharset(respCharset);
        } else if (StringHelper.isNotEmpty(getCharset())) {
            result.setCharset(getCharset());
        }
        // <300算成功处理
        if (result.getStatus() < HttpURLConnection.HTTP_MULT_CHOICE) {
            try (InputStream inStream = getInputStream(httpConn)) {
                result.setData(IOHelper.readBytes(inStream));
            } catch (IOException e) {
                throw e;
            }
        } else {
            if (null != httpConn.getErrorStream()) {
                try (InputStream inStream = httpConn.getErrorStream()) {
                    result.setData(IOHelper.readBytes(inStream));
                } catch (IOException e) {
                    throw e;
                }
            } else {
                result.setMessage(httpConn.getResponseMessage());
            }
            log.error("请求失败," + result);
        }
        return result;
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
        boolean inited = helper.initConnect(url, HttpMethod.GET);
        if (inited) {
            return helper.sendRequest();
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
        boolean inited = helper.initConnect(url, HttpMethod.POST);
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
    public static <T extends Serializable> HttpResponse postJson(String url, T entity) {
        HttpHelper helper = new HttpHelper();
        boolean inited = helper.initConnect(url, HttpMethod.POST);
        if (inited) {
            return helper.sendJsonRequest(entity);
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
    public static <T> HttpResponse postXml(String url, T entity) {
        HttpHelper helper = new HttpHelper();
        boolean inited = helper.initConnect(url, HttpMethod.POST);
        if (inited) {
            return helper.sendXmlRequest(entity);
        } else {
            return new HttpResponse(HttpURLConnection.HTTP_UNAVAILABLE, "建立连接失败");
        }
    }

    private void setConnectionProperty() throws ProtocolException, URISyntaxException {
        conn.setRequestMethod(getMethod().name());
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
        conn.setDoOutput(true);
        // 设置cookie
        String cookies = getCookies(requestUrl.toURI());
        if (StringHelper.isNotEmpty(cookies)) {
            conn.setRequestProperty("Cookie", cookies);
        }
        // 设置请求头
        if (getHeaders() != null) {
            for (Entry<String,String> entry : getHeaders().entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (getMethod() == HttpMethod.POST) {
            if (StringHelper.isEmpty(conn.getRequestProperty(headContentType))) {
                conn.setRequestProperty(headContentType, contentTypePost);
            }
        }
    }

    /**
     * 默认https域名校验,信任所有
     */
    private class TrustAnyHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * https证书管理
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

    private SSLSocketFactory initDefaultSSLSocketFactory() {
        TrustManager[] tm = { new HttpHelper().new TrustAnyTrustManager() };
        return HttpHelper.initSSLSocketFactory("TLS", tm);
    }

    /**
     * 指定协议和认证管理生成SSLSocketFactory
     * 
     * @param protocol HTTPS安全协议SSL/TLS...
     * @param tms 认证管理器TrustManager
     * @return SSLSocketFactory
     */
    public static SSLSocketFactory initSSLSocketFactory(String protocol, TrustManager[] tms) {
        try {
            SSLContext sslContext = SSLContext.getInstance(protocol, "SunJSSE");
            sslContext.init(null, tms, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class ByteRequestBodyHandler implements RequestBodyHandler<byte[]> {

        @Override
        public byte[] handle(byte[] requestData, RequestProperty requestProperty) {
            return requestData;
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
        // List<HttpCookie> cookiess = cookieManager.getCookieStore().getCookies();
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

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public void setTrustVerifier(TrustAnyHostnameVerifier trustVerifier) {
        this.trustVerifier = trustVerifier;
    }

    public int getConnectionTimeout() {
        return requestProperty.getConnectionTimeout();
    }

    /**
     * 设置连接超时,单位毫秒
     * 
     * @param connectionTimeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.requestProperty.setConnectionTimeout(connectionTimeout);
    }

    public int getReadTimeout() {
        return requestProperty.getReadTimeout();
    }

    /**
     * 设置读取超时,单位毫秒
     * 
     * @param readTimeout
     */
    public void setReadTimeout(int readTimeout) {
        this.requestProperty.setReadTimeout(readTimeout);
    }

    public String getCharset() {
        return this.requestProperty.getCharset();
    }

    /**
     * 设置请求编码,默认使用utf-8
     * 
     * @throws IllegalArgumentException 不支持字符集
     */
    public void setCharset(String charset) {
        if (CharsetHelper.isSupported(charset)) {
            this.requestProperty.setCharset(charset);
        } else {
            throw new IllegalArgumentException("不支持的字符集" + charset);
        }
    }

    public HttpMethod getMethod() {
        return this.requestProperty.getMethod();
    }

    public void setMethod(HttpMethod method) {
        this.requestProperty.setMethod(method);
    }

    public Map<String,String> getHeaders() {
        return this.requestProperty.getHeaders();
    }

    /**
     * 设置http请求头
     * 
     * @param headers
     */
    public void setHeaders(Map<String,String> headers) {
        if (this.requestProperty.getHeaders() == null) {
            this.requestProperty.setHeaders(headers);
        } else {
            this.requestProperty.getHeaders().putAll(headers);
        }
    }

    /**
     * 增加http请求头
     * 
     * @param headField header field name
     * @param value header field value
     */
    public void addHeader(String headField, String value) {
        if (this.requestProperty.getHeaders() == null) {
            this.requestProperty.setHeaders(new HashMap<String,String>());
        }
        this.requestProperty.getHeaders().put(headField, value);
    }

    /**
     * 返回请求头值
     * 
     * @param headField 请求头名称
     * @return
     */
    public String getHeader(String headField) {
        return this.requestProperty.getHeader(headField);
    }
}
