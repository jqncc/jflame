package org.jflame.toolkit.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.toolkit.exception.BusinessException;
import org.jflame.toolkit.file.PropertiesHelper;

/**
 * 默认实现 从properties文件加载配置参数.<br>
 * 
 * @author yucan.zhang
 */
public class PropertiesConfig extends BaseParamStrategy {

    private final Logger log = LoggerFactory.getLogger(PropertiesConfig.class);

    private String[] propertiesFiles;
    private Properties properties;
    private boolean valueConvert = false;// 是否对属性值做转换,

    /*   public PropertiesConfig() {
        properties = new Properties();
    }*/

    /**
     * 构造函数,指定配置文件
     * 
     * @param propertiesFiles 配置文件路径
     * @throws BusinessException
     */
    public PropertiesConfig(String... propertiesFiles) throws BusinessException {
        if (propertiesFiles == null) {
            throw new IllegalArgumentException("请设置propertiesFiles");
        }
        this.propertiesFiles = propertiesFiles;
        try {
            loadFromProperties();
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    public PropertiesConfig(Properties properties) {
        this.properties = properties;
    }

    /**
     * 从属性配置文件加载参数
     * 
     * @throws IOException
     */
    protected void loadFromProperties() throws IOException {
        log.info("从属性文件加载参数:" + Arrays.toString(propertiesFiles));
        PropertiesHelper loader = new PropertiesHelper(propertiesFiles);
        if (loader != null) {
            properties = loader.getProperties();
            if (valueConvert) {
                for (Entry<Object,Object> kv : properties.entrySet()) {
                    properties.put(kv.getKey(), convertProperty((String) kv.getKey(), kv.getValue()));
                }
            }
        }

    }

    /**
     * 设置参数
     * 
     * @param paramKey 参数名
     * @param value 参数值
     */
    public void addParam(Object paramKey, Object value) {
        properties.put(paramKey, value);
    }

    /**
     * 设置多个参数
     * 
     * @param m 参数map
     */
    public void addParams(Map<? extends Object,? extends Object> m) {
        properties.putAll(m);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String getParam(ConfigKey configKey) {
        return properties.getProperty(configKey.getName());
    }

    public void clear() {
        properties.clear();
    }

    /**
     * 实现属性值转换,如解密属性
     * 
     * @param propertyName 属性名
     * @param propertyValue 属性值
     * @return
     */
    protected Object convertProperty(String propertyName, Object propertyValue) {
        return propertyValue;
    }

    public void setValueConvert(boolean valueConvert) {
        this.valueConvert = valueConvert;
    }

    public void setPropertiesFiles(String[] propertiesFiles) {
        this.propertiesFiles = propertiesFiles;
    }

}
