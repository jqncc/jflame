package org.jflame.toolkit.exception;

/**
 * 访问权限异常
 * 
 * @author zyc
 * @version 1.0
 */
public class PermissionException extends RuntimeException {

    private static final long serialVersionUID = 2305288533896928521L;

    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String message, Throwable error) {
        super(message, error);
    }

    public PermissionException(Throwable error) {
        super(error);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
