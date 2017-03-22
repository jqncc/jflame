package org.jflame.toolkit.codec;

/**
 * 编码转码异常
 * 
 * @author yucan.zhang
 */
public class TranscodeException extends Exception {

    private static final long serialVersionUID = 1L;

    public TranscodeException() {
        super();
    }

    public TranscodeException(String message) {
        super(message);
    }

    public TranscodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TranscodeException(Throwable cause) {
        super(cause);
    }
}
