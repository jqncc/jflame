package org.jflame.context.cache.redis;

import org.jflame.commons.exception.DataAccessException;

/**
 * redis处理异常类
 * 
 * @author zyc
 */
public class RedisAccessException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public RedisAccessException() {
        super();
    }

    public RedisAccessException(String message) {
        super(message);
    }

    public RedisAccessException(Throwable exception) {
        super(exception);
    }

    public RedisAccessException(String message, Throwable exception) {
        super(message, exception);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
