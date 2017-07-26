package org.jflame.toolkit.file;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 国际化资源文件操作工具类
 */
public class ResourceBundleHelper {

    private ResourceBundle resourceBundle;

    /**
     * 按系统默认地区加载资源文件
     * 
     * @param resourceName 文件名
     */
    public ResourceBundleHelper(String resourceName) {
        resourceBundle = ResourceBundle.getBundle(resourceName);
    }

    /**
     * 指定地区加载资源文件
     * 
     * @param resourceName 文件名
     * @param locale 区域
     */
    public ResourceBundleHelper(String resourceName, Locale locale) {
        resourceBundle = ResourceBundle.getBundle(resourceName, locale);
    }

    /**
     * 获取消息
     * 
     * @param code 消息名
     * @return
     */
    public String getMessage(String code) {
        return resourceBundle.getString(code);
    }

    /**
     * 获取消息,如果消息为空则返回默认值
     * 
     * @param code 消息名
     * @param defaultMessage 默认值
     * @return
     */
    public String getMessage(String code, String defaultMessage) {
        String result = getMessage(code);
        return result == null ? defaultMessage : result;
    }

    /**
     * 获取消息,使用给定参数格式化消息
     * 
     * @param code 消息名
     * @param args 要替换的参数
     * @return
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, null);
    }

    /**
     * 获取消息,使用给定参数格式化消息,为空则返回默认值
     * 
     * @param code 消息名
     * @param args 要替换的参数
     * @param defaultValue 默认值
     * @return
     */
    public String getMessage(String code, Object[] args, String defaultValue) {
        String result = getMessage(code);
        if (result != null && ArrayUtils.isNotEmpty(args)) {
            result = MessageFormat.format(result, args);
        }
        return result == null ? defaultValue : result;
    }

}
