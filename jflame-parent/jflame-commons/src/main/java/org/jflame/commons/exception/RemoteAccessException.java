package org.jflame.commons.exception;

/**
 * 远程访问异常,如http,ftp等远程请求时出现的异常
 * 
 * @author zyc
 */
@SuppressWarnings("serial")
public class RemoteAccessException extends Exception {

    private int statusCode;// 异常状态码

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public RemoteAccessException(String message) {
        super(message);
    }

    public RemoteAccessException(String message, int statusCode) {
        super(message + " status code:" + statusCode);
        this.statusCode = statusCode;
    }

    public RemoteAccessException(Throwable exception) {
        super(exception);
    }

    public RemoteAccessException(int statusCode, Throwable exception) {
        super("",exception);
        this.statusCode = statusCode;
    }
    
    public RemoteAccessException(String message, Throwable exception) {
        super(message, exception);
    }

    public RemoteAccessException(String message, int statusCode, Throwable exception) {
        super(message + " status code:" + statusCode, exception);
        this.statusCode = statusCode;
    }
}
