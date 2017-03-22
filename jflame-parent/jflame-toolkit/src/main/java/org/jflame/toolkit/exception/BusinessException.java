package org.jflame.toolkit.exception;

/**
 * 业务层异常
 * 
 * @author zyc
 * @version 1.0
 */
public class BusinessException extends Exception {

    private static final long serialVersionUID = 8128513200524494718L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable error) {
        super(message, error);
    }

    public BusinessException(Throwable error) {
        super(error);
    }

}
