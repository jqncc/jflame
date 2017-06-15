package org.jflame.toolkit.exception;

/**
 * 业务层异常
 * 
 * @author zyc
 * @version 1.0
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 8128513200524494718L;
    private int statusCode;// 异常状态码

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable error) {
        super(message, error);
    }

    public BusinessException(Throwable error) {
        super(error);
    }

    public BusinessException(String message, int statusCode) {
        super(message + " status code:" + statusCode);
        this.statusCode = statusCode;
    }

    public BusinessException(int statusCode, Throwable exception) {
        super("", exception);
        this.statusCode = statusCode;
    }

    public BusinessException(String message, int statusCode, Throwable exception) {
        super(message + " status code:" + statusCode, exception);
        this.statusCode = statusCode;
    }
}
