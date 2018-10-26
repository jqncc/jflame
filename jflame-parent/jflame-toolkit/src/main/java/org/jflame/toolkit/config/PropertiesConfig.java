package org.jflame.toolkit.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.jflame.toolkit.exception.BusinessException;
import org.jflame.toolkit.file.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认实现 从properties文件加载配置参数.<br>
 * 
 * @author yucan.zhang
 */
public class PropertiesConfig extends BaseParamStrategy implements ISysConfig {

    private static final Logger log = LoggerFactory.getLogger(PropertiesConfig.class);

    private String[] propertiesFiles;
    private Properties properties;
    private static AtomicBoolean isLoaded = new AtomicBoolean(false);
    private boolean valueConvert = false;// 是否对属性值做转换,

    public PropertiesConfig() {

    }

    /**
     * 构造函数,指定配置文件
     * 
     * @param configFiles 配置文件名
     * @throws BusinessException
     */
    public PropertiesConfig(String... configFiles) throws BusinessException {
        propertiesFiles = configFiles;
        loadConfig();
    }

    @Override
    public synchronized void loadConfig() throws BusinessException {
        if (!isLoaded.get()) {
            try {
                loadFromProperties();
            } catch (Exception e) {
                throw new BusinessException(e);
            }
            isLoaded.set(true);
        }
    }

    @Override
    public synchronized void reloadConfig() throws BusinessException {
        log.info("重新加载配置参数...");
        if (isLoaded.get()) {
            properties.clear();
            isLoaded.set(false);
        }
        loadConfig();
    }

    @Override
    public Object getParam(String paramKey) {
        return properties.get(paramKey);
    }

    /**
     * 从属性配置文件加载参数
     * 
     * @throws IOException
     */
    protected void loadFromProperties() throws IOException {
        if (ArrayUtils.isNotEmpty(propertiesFiles)) {
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
        } else {
            log.warn("未设置参数配置文件");
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

    @Override
    public String getTextParam(String paramKey) {
        return properties.getProperty(paramKey);
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
