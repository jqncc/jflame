package org.jflame.web.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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
 * 加载系统配置参数.
 * @author yucan.zhang
 *
 */
public class BaseSysConfig implements ISysConfig {
    
    private static final Logger log = LoggerFactory.getLogger(BaseSysConfig.class);
    
    private String[] propertiesFiles;
    private static final Map<String, Object> paramMap=new HashMap<>();
    private static AtomicBoolean isLoaded=new AtomicBoolean(false);
    
    public BaseSysConfig(){}
    
    public BaseSysConfig(String... configFiles) throws BusinessException{
        propertiesFiles=configFiles;
        loadConfig();
    }

    @Override
    public synchronized void loadConfig() throws BusinessException {
        log.info("加载配置参数...");
        if (!isLoaded.get()) {
            try {
                loadFromConfigFile();
                loadFromDb();
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
           paramMap.clear();
        }
        loadConfig();
    }
    
    @Override
    public Map<String,Object> getAllParams() {
        return paramMap;
    }

    @Override
    public Object getParam(String paramKey) {
        return paramMap.get(paramKey);
    }

    @Override
    public String getTextParam(String paramKey) {
        return String.valueOf(paramMap.get(paramKey));
    }
    
    public void setPropertiesFiles(String[] propertiesFiles) {
        this.propertiesFiles = propertiesFiles;
    }

    /**
     * 从属性配置文件加载参数
     * @throws IOException 
     */
    protected void loadFromConfigFile() throws IOException{
        if (ArrayUtils.isNotEmpty(propertiesFiles)) {
            PropertiesHelper loader = new PropertiesHelper(propertiesFiles);
            if (loader!=null) {
                Properties properties=loader.getProperties();
                if (properties!=null&&!properties.isEmpty()) {
                    for (Entry<Object,Object> entry: properties.entrySet()) {
                        paramMap.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                }
            }
           
            log.info("从属性文件加载参数:"+Arrays.toString(propertiesFiles));
        }else{
            log.warn("未设置参数配置文件");
        }
    }
  
    protected void loadFromDb(){}
}
