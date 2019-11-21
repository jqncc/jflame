package org.jflame.commons.exception;

/**
 * javabean操作异常类,如果bean的内省,反射读取或设置bean属性等操作出现异常.
 * 
 * @author zyc
 */
public class BeanAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BeanAccessException() {
        super();
    }

    public BeanAccessException(String message) {
        super(message);
    }

    public BeanAccessException(Throwable exception) {
        super(exception);
    }

    public BeanAccessException(String message, Throwable exception) {
        super(message, exception);
    }
}
