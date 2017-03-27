package org.jflame.toolkit.net;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.jflame.toolkit.exception.RemoteAccessException;

/**
 * http返回结果处理接口
 * 
 * @author yucan.zhang
 * @param <T> 结果类型
 */
public interface IResponseResultHandler<T> {

    T handle(HttpURLConnection httpConn) throws IOException, RemoteAccessException;
}
