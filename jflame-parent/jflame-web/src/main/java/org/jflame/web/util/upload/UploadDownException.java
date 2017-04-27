package org.jflame.web.util.upload;
/**
 * 文件上传或下载异常
 * @author yucan.zhang
 *
 */
@SuppressWarnings("serial")
public class UploadDownException extends RuntimeException {

    public UploadDownException(String message) {
        super(message);
    }

    public UploadDownException(String message, Throwable error) {
        super(message, error);
    }

    public UploadDownException(Throwable error) {
        super(error);
    }
}
