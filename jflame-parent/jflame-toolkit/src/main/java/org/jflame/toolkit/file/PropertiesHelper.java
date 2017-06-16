package org.jflame.toolkit.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jflame.toolkit.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties文件载入工具类. 可载入多个properties文件. 相同的属性在最后载入的文件中的值将会覆盖之前的值，但以System的Property优先.
 * 
 * @author yucan.zhang
 */
public final class PropertiesHelper {

    private static final Logger log = LoggerFactory.getLogger(PropertiesHelper.class);
    private final Properties properties=new Properties();
    private final Pattern varPattern = Pattern.compile("\\$\\{([^\\}]+)\\}");

    /**
     * 构造函数.
     * 
     * @param resourcesPaths 相对于classpath的文件路径
     * @throws IOException 
     */
    public PropertiesHelper(String... resourcesPaths) throws IOException {
        loadProperties(resourcesPaths);
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
     * 载入多个文件
     * 
     * @param resourcesPaths 资源文件路径,路径以/开头从classpath下去，相对路径从此类所在的包下取资源
     * @throws IOException 
     */
    private void loadProperties(String... resourcesPaths) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        for (String location : resourcesPaths) {
            if (StringHelper.isNotEmpty(location)) {
                //修正下路径,classLoader不以/开头
                if (location.charAt(0) == '/') {
                    location = location.substring(1);
                }
                try (InputStream is = classLoader.getResourceAsStream(location)) {
                    if (is != null) {
                        properties.load(is);
                    }
                } catch (IOException ex) {
                    log.error("加载资源文件失败" + location, ex);
                    throw ex;
                }
            }
        }
        // 替换变量${}
        if (!properties.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            for (Entry<Object,Object> entry : properties.entrySet()) {
                String value = properties.getProperty((String) entry.getKey());
                Matcher matcher = varPattern.matcher(value);
                buffer.setLength(0);
                while (matcher.find()) {
                    String matcherKey = matcher.group(1);
                    String matchervalue = properties.getProperty(matcherKey);
                    // 找系统环境变量
                    if (matchervalue == null) {
                        matchervalue = System.getProperty(matcherKey);
                    }
                    if (matchervalue != null) {
                        //替换特殊字符\$
                        if (StringHelper.containsAny(matchervalue, '\\', '$')) {
                            matcher.appendReplacement(buffer,
                                    matchervalue.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$"));
                        } else {
                            matcher.appendReplacement(buffer, matchervalue);
                        }
                    }
                }
                matcher.appendTail(buffer);
                properties.put(entry.getKey(), buffer.toString());
            }
        } else {
            log.warn("未加载到任何属性");
        }
    }
    
}
