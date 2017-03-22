package org.jflame.toolkit.excel;

/**
 * excel文件处理异常.
 * 
 * @author zyc
 */
public class ExcelAccessException extends RuntimeException {
    private static final long serialVersionUID = -3558803785682777L;

    public ExcelAccessException(String message) {
        super(message);
    }

    public ExcelAccessException(String message, Throwable error) {
        super(message, error);
    }

    public ExcelAccessException(Throwable error) {
        super(error);
    }
}
