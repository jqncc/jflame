package org.jflame.commons.util.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.model.Chars;
import org.jflame.commons.util.IOHelper;
import org.jflame.commons.util.StringHelper;

/**
 * Properties文件载入工具类. 可载入多个properties文件. 相同的属性在最后载入的文件中的值将会覆盖之前的值
 * 
 * @author yucan.zhang
 */
public final class PropertiesHelper {

    private static final Logger log = LoggerFactory.getLogger(PropertiesHelper.class);
    private final Properties properties = new Properties();
    private final Pattern varPattern = Pattern.compile("\\$\\{([^\\}]+)\\}");

    /**
     * 构造函数.
     * <p>
     * 文件路径以"classpath:"开头或是相对路径则从当前classpath查找,否则作为绝对路径.示例:<br>
     * 相对路径:"classpath:jdbc.properties","config/jdbc.properties";<br>
     * 绝对路径:"d:/jdbc.properties","/home/user/jdbc.properties";
     * </p>
     * 
     * @param resourcesPaths 资源文件路径
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
        return properties.getProperty(key);
    }

    /**
     * 取出String类型的属性
     * 
     * @param key 属性名
     */
    public String getProperty(String key) {
        String value = getValue(key);
        return value != null ? value.trim() : value;
    }

    /**
     * 取出String类型的属性
     * 
     * @param key 属性名
     * @param defaultValue 默认值
     */
    public String getProperty(String key, String defaultValue) {
        String value = getValue(key);
        return value != null ? value.trim() : defaultValue;
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
     * 读取多个资源文件.
     * <p>
     * 文件路径以"classpath:"开头或是相对路径则从当前classpath查找,否则作为绝对路径.示例:<br/>
     * 相对路径:"classpath:jdbc.properties","config/jdbc.properties"<br/>
     * 绝对路径:"d:/jdbc.properties","/home/user/jdbc.properties"
     * </p>
     * 
     * @param resourcesPaths 资源文件路径
     * @throws IOException
     */
    private void loadProperties(String... resourcesPaths) throws IOException {
        for (String location : resourcesPaths) {
            if (log.isDebugEnabled()) {
                log.debug("加载资源文件{}", location);
            }
            InputStream inStream = null;
            location = location.replaceFirst("classpath:/?", "");

            // 非绝对路径从classpath读取
            if (isAbsolute(location)) {
                inStream = new FileInputStream(location);
            } else {
                inStream = FileHelper.readFileFromClassPath(location);
            }
            try {
                if (inStream != null) {
                    properties.load(inStream);
                }
            } catch (IOException ex) {
                log.error("加载资源文件失败" + location, ex);
                throw ex;
            } finally {
                IOHelper.closeQuietly(inStream);
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
                        // 替换特殊字符\$
                        if (StringHelper.containsAny(matchervalue, '\\', '$')) {
                            matcher.appendReplacement(buffer, matchervalue.replaceAll("\\\\", "\\\\\\\\")
                                    .replaceAll("\\$", "\\\\\\$"));
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

    public void store(Path filePath) throws IOException {
        properties.store(Files.newOutputStream(filePath), null);
    }

    boolean isAbsolute(String path) {
        Path p = Paths.get(path);
        if (p.isAbsolute()) {
            return true;
        }
        // 非windows系统,/开头视为绝对路径
        if (!SystemUtils.IS_OS_WINDOWS && path.charAt(0) == Chars.SLASH) {
            return true;
        }
        return false;
    }

}
