package org.jflame.toolkit.net;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.jflame.toolkit.common.bean.CallResult;
import org.jflame.toolkit.util.StringHelper;

/**
 * http请求返回结果
 * 
 * @author yucan.zhang
 */
public class HttpResponse extends CallResult {

    private static final long serialVersionUID = -8303137663872800766L;
    private Map<String,List<String>> headers;// http headers
    private String charset;

    public HttpResponse() {
    }

    public HttpResponse(int status) {
        super(status);
    }

    public HttpResponse(int status, String message) {
        super(status, message);
    }

    public Map<String,List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String,List<String>> headers) {
        this.headers = headers;
    }

    /**
     * 以文本类型返回实际请求结果.结果不能转为文本时无效
     * 
     * @return
     */
    public String getDataAsText() {
        if (getData() != null) {
            if (getData() instanceof String) {
                return (String) getData();
            } else {
                byte[] resultBytes = getDataAsBytes();
                if (resultBytes != null) {
                    try {
                        return StringHelper.isNotEmpty(charset) ? new String(resultBytes, charset)
                                : StringHelper.getUtf8String(resultBytes);
                    } catch (UnsupportedEncodingException e) {
                        throw new ClassCastException("不能使用指定编码" + charset + "转为字符串");
                    }
                }
            }
        }
        return null;
    }

    /**
     * 以byte[]类型返回实际请求结果.结果不能转为byte[]时无效
     * 
     * @return
     */
    public byte[] getDataAsBytes() {
        if (getData() != null) {
            return (byte[]) getData();
        }
        return null;
    }

}
