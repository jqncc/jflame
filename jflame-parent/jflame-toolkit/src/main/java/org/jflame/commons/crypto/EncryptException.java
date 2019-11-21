package org.jflame.commons.crypto;

/**
 * 解密加密异常类.
 * 
 * @author zyc CreateDate:2015年3月10日上午11:35:53
 */
public class EncryptException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EncryptException(String message) {
        super(message);
    }

    public EncryptException(String message, Throwable error) {
        super(message, error);
    }

    public EncryptException(Throwable error) {
        super(error);
    }
}
