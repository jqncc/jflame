package org.jflame.toolkit.exception;

/**
 * 值转换异常
 * 
 * @author zyc
 * @version 1.0
 */
public class ConvertException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConvertException() {
        super();
    }

    public ConvertException(String message) {
        super(message);
    }

    public ConvertException(String message, Throwable error) {
        super(message, error);
    }

    public ConvertException(Throwable error) {
        super(error);
    }

}
