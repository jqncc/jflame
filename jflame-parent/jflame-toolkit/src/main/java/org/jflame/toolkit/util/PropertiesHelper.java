package org.jflame.toolkit.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Properties文件载入工具类. 可载入多个properties文件. 相同的属性在最后载入的文件中的值将会覆盖之前的值，但以System的Property优先.
 * 
 * @author yucan.zhang
 */
public final class PropertiesHelper {

    private final Properties properties;

    public PropertiesHelper(String... resourcesPaths) throws IOException {
        properties = loadProperties(resourcesPaths);
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * 取出属性值，作为字符串返回.
     * 
     * @param key 属性名
     */
    private String getValue(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return systemProperty;
        }
        return properties.getProperty(key);
    }

    /**
     * 取出String类型的属性,如果都为Null则抛出异常.
     * 
     * @param key 属性名
     */
    public String getProperty(String key) {
        String value = getValue(key);
        if (value == null) {
            throw new NoSuchElementException("未配置属性:" + key);
        }
        return value;
    }

    /**
     * 取出String类型的属性.如果都为Null则返回Default值.
     * 
     * @param key 属性名
     * @param defaultValue 默认值
     */
    public String getProperty(String key, String defaultValue) {
        String value = getValue(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 取出Integer类型的属性.如果都为Null或内容错误则抛出异常.
     * 
     * @param key 属性名
     */
    public Integer getInteger(String key) {
        String value = getValue(key);
        if (value == null) {
            throw new NoSuchElementException("未配置属性:" + key);
        }
        return Integer.valueOf(value);
    }

    /**
     * 取出Integer类型的属性.如果都为Null则返回Default值，如果内容错误则抛出异常
     * 
     * @param key 属性名
     * @param defaultValue 默认值
     */
    public Integer getInteger(String key, Integer defaultValue) {
        String value = getValue(key);
        return value != null ? Integer.valueOf(value) : defaultValue;
    }

    /**
     * 取出Double类型的属性.如果都为Null或内容错误则抛出异常.
     * 
     * @param key 属性名
     */
    public Double getDouble(String key) {
        String value = getValue(key);
        if (value == null) {
            throw new NoSuchElementException("未配置属性:" + key);
        }
        return Double.valueOf(value);
    }

    /**
     * 取出Double类型的属性.如果都为Null则返回Default值，如果内容错误则抛出异常
     * 
     * @param key 属性名
     * @param defaultValue 默认值
     */
    public Double getDouble(String key, Integer defaultValue) {
        String value = getValue(key);
        return value != null ? Double.valueOf(value) : defaultValue;
    }

    /**
     * 取出Boolean类型的属性.如果都为Null抛出异常,如果内容不是true/false则返回false.
     * 
     * @param key 属性名
     */
    public Boolean getBoolean(String key) {
        String value = getValue(key);
        if (value == null) {
            throw new NoSuchElementException("未配置属性:" + key);
        }
        return Boolean.valueOf(value);
    }

    /**
     * 取出Boolean类型的Propert.如果都为Null则返回Default值,如果内容不为true/false则返回false.
     * 
     * @param key 属性名
     * @param defaultValue 默认值
     */
    public Boolean getBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        return value != null ? Boolean.valueOf(value) : defaultValue;
    }

    /**
     * 查找指定key的属性是否存在
     * 
     * @param key 属性名
     * @return
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * 载入多个文件, 文件路径使用Spring Resource格式.
     * 
     * @param resourcesPaths 资源文件路径
     * @throws IOException 文件读取失败
     */
    private Properties loadProperties(String... resourcesPaths) throws IOException {
        Properties props = new Properties();

        for (String location : resourcesPaths) {
            // logger.debug("Loading properties file from path:{}", location);
            try (InputStream is = PropertiesHelper.class.getResourceAsStream(location)) {
                props.load(is);
            } catch (IOException ex) {
                // logger.info("Could not load properties from path:{}, {} ", location, ex.getMessage());
                throw ex;
            }
        }
        return props;
    }
}
