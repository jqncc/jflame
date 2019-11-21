package org.jflame.commons.exception;

/**
 * 序列化异常
 * 
 * @author yucan.zhang
 */
public class SerializeException extends RuntimeException {

    private static final long serialVersionUID = -2718846636566866315L;

    public SerializeException() {
        super();
    }

    public SerializeException(String msg) {
        super(msg);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }

    public SerializeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
