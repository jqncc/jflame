package org.jflame.context.spring;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

/**
 * spring ApplicationContext holder
 *  
 * @author zyc
 */
public final class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext springContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.springContext = applicationContext;
    }

    /**
     * 返回spring上下文环境对象
     * 
     * @return ApplicationContext
     */
    public static ApplicationContext getSpringContext() {
        return springContext;
    }

    
    /**
     * 按名称获取spring bean
     * 
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return springContext.getBean(name);
    }

    /**
     * 按类型获取spring bean
     * 
     * @param requiredType
     * @return
     */
    public static <T> T getBean(Class<T> requiredType) {
        return springContext.getBean(requiredType);
    }

    public static String getMessage(String code) {
        return springContext.getMessage(code, null, Locale.getDefault());
    }
    
    /**
     * 返回多语言资源文件中的文本信息,使用系统默认区域
     * 
     * @param code key
     * @param args 变量参数
     * @return
     */
    public static String getMessage(String code, Object[] args) {
        return springContext.getMessage(code, args, Locale.getDefault());
    }

    /**
     * 返回多语言资源文件中的文本信息,使用系统默认区域
     * 
     * @param code key
     * @param args 变量参数
     * @param defaultMessage 缺省文本
     * @return
     */
    public static String getMessage(String code, Object[] args, String defaultMessage) {
        return springContext.getMessage(code, args, defaultMessage, Locale.getDefault());
    }

    /**
     * 返回多语言资源文件中的文本信息
     * 
     * @param code key
     * @param args 变量参数
     * @param defaultMessage 缺省文本
     * @param locale 区域
     * @return
     */
    public static String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return springContext.getMessage(code, args, defaultMessage, locale);
    }

    /**
     * 发布事件到spring
     * @param event
     */
    public static void pushEvent(ApplicationEvent event) {
        springContext.publishEvent(event);
    }
    
}
