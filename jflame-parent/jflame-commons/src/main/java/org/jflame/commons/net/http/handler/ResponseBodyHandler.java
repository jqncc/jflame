package org.jflame.commons.net.http.handler;

import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.net.http.HttpResponse;

/**
 * http返回结果处理接口
 * 
 * @author yucan.zhang
 * @param <T> 要转换的结果类型
 */
public interface ResponseBodyHandler<T> {

    T handle(HttpResponse response) throws ConvertException;
}
