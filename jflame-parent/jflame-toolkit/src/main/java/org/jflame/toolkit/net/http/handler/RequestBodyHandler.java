package org.jflame.toolkit.net.http.handler;

import org.jflame.toolkit.net.http.RequestProperty;

/**
 * 请求参数转为http body参数处理器接口
 * 
 * @author yucan.zhang
 * @param <T> 参数原始类型
 */
public interface RequestBodyHandler<T> {

    /**
     * 请求参数处理转换
     * 
     * @param requestData 参数
     * @param requestProperty 请求属性
     * @return
     */
    byte[] handle(T requestData, RequestProperty requestProperty);
}
