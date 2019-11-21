package org.jflame.commons.csv;

/**
 * csv文件处理异常.
 * 
 * @author zyc
 */
public class CsvAccessException extends RuntimeException {

    private static final long serialVersionUID = -3558803785682777L;

    public CsvAccessException(String message) {
        super(message);
    }

    public CsvAccessException(String message, Throwable error) {
        super(message, error);
    }

    public CsvAccessException(Throwable error) {
        super(error);
    }
}
