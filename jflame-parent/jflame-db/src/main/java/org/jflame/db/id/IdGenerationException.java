package org.jflame.db.id;

/**
 * id生成异常
 * @author yucan.zhang
 *
 */
public class IdGenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IdGenerationException() {
        super();
    }

    public IdGenerationException(String message) {
        super(message);
    }

    public IdGenerationException(Throwable exception) {
        super(exception);
    }

    public IdGenerationException(String message, Throwable exception) {
        super(message, exception);
    }
}
