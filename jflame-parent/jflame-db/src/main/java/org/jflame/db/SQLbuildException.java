package org.jflame.db;

/**
 * 生成sql时的异常
 * 
 * @author zyc
 */
@SuppressWarnings("serial")
public class SQLbuildException extends RuntimeException
{
    public SQLbuildException()
    {
        super();
    }

    public SQLbuildException(String message)
    {
        super(message);
    }

    public SQLbuildException(Throwable exception)
    {
        super(exception);
    }

    public SQLbuildException(String message, Throwable exception)
    {
        super(message, exception);
    }
}
