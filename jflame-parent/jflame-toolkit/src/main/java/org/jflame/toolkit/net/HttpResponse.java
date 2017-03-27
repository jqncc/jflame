package org.jflame.toolkit.net;

import java.util.List;
import java.util.Map;

import org.jflame.toolkit.common.bean.CallResult;

/**
 * http请求返回结果
 * 
 * @author yucan.zhang
 */
public class HttpResponse extends CallResult {

    private static final long serialVersionUID = -8303137663872800766L;
    private Map<String,List<String>> headers;// http headers

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
     * @return
     */
    public String getDataAsText(){
        if(getData()!=null){
            return (String)getData();
        }
        return null;
    }
    
    /**
     * 以byte[]类型返回实际请求结果.结果不能转为byte[]时无效
     * @return
     */
    public byte[] getDataAsBytes(){
        if(getData()!=null){
            return (byte[])getData();
        }
        return null;
    }
    
}
