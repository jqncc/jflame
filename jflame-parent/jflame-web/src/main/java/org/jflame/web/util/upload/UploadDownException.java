package org.jflame.web.util;
/**
 * 文件上传异常
 * @author yucan.zhang
 *
 */
@SuppressWarnings("serial")
public class UploadException extends RuntimeException {

    public UploadException(String message) {
        super(message);
    }

    public UploadException(String message, Throwable error) {
        super(message, error);
    }

    public UploadException(Throwable error) {
        super(error);
    }
}
