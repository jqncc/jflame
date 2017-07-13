package org.jflame.toolkit.net.http;

import java.util.Map;

import org.jflame.toolkit.net.HttpHelper.HttpMethod;

public class RequestProperty {

    private int connectionTimeout;
    private int readTimeout;
    private String charset;
    private HttpMethod method;
    private Map<String,String> headers;

    public RequestProperty() {
    }

    /**
     * 构造函数
     * 
     * @param connectionTimeout 连接超时
     * @param readTimeout 读取超时
     * @param charset 字符编码
     */
    public RequestProperty(int connectionTimeout, int readTimeout, String charset) {
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.charset = charset;
    }

    /**
     * 构造函数
     * 
     * @param connectionTimeout 连接超时
     * @param readTimeout 读取超时
     * @param charset 字符编码
     * @param method 请求方式
     * @param headers 请求头
     */
    public RequestProperty(int connectionTimeout, int readTimeout, String charset, HttpMethod method,
            Map<String,String> headers) {
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.charset = charset;
        this.method = method;
        this.headers = headers;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
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
     * 返回请求头值
     * 
     * @param headField 请求头名称
     * @return
     */
    public String getHeader(String headField) {
        if (headers != null) {
            return headers.get(headField);
        }
        return null;
    }

    public void setHeaders(Map<String,String> headers) {
        this.headers = headers;
    }

}
