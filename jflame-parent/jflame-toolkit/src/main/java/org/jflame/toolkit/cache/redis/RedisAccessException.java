package org.jflame.toolkit.cache.redis;

/**
 * redis处理异常类
 * 
 * @author zyc
 */
public class RedisAccessException extends RuntimeException {

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
}
