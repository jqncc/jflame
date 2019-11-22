package org.jflame.context.spring;

import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import org.jflame.commons.config.PropertiesConfigHolder;
import org.jflame.commons.crypto.BaseEncryptor.Algorithm;
import org.jflame.commons.crypto.SymmetricEncryptor;

/**
 * 继承spring属性加载器，加入自定义行为：<br>
 * 1.支持将属性注入到自己的参数配置类，方便代码直接获取 <br>
 * 2.属性aes加密
 * 
 * @author yucan.zhang
 */
public class MyPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

    private String[] encrptyKeys;
    private SymmetricEncryptor aes;
    private boolean holdProperty = false;// 是否将spring加载的属性设置到PropertiesConfigHolder
    private boolean enableEncrypt = false; // 是否启用加密属性解析
    private String password;// 解密属性密码
    private byte[] passwordBytes;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
            throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        if (holdProperty) {
            PropertiesConfigHolder.loadProperties(props);
        }
    }

    @Override
    protected String convertProperty(String propertyName, String propertyValue) {
        if (enableEncrypt && encrptyKeys != null) {
            if (ArrayUtils.contains(encrptyKeys, propertyName)) {
                return aes.dencryptHex(propertyValue, passwordBytes, null);
            }
        }
        return propertyValue;
    }

    /**
     * 加密的属性名
     * 
     * @param encrptyKeys
     */
    public void setEncrptyKeys(String[] encrptyKeys) {
        this.encrptyKeys = encrptyKeys;
    }

    /**
     * 设置是否将属性注入到属性配置类PropertiesConfigHolder
     * 
     * @param holdProperty
     */
    public void setHoldProperty(boolean holdProperty) {
        this.holdProperty = holdProperty;
    }

    public void setEnableEncrypt(boolean enableEncrypt) {
        this.enableEncrypt = enableEncrypt;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (enableEncrypt) {
            aes = new SymmetricEncryptor(Algorithm.AES);
            // 补全16位密码,对字符串base64编码后做为aes密钥
            if (password.length() < 16) {
                password = StringUtils.rightPad(password, 16, 'x');
            }

            passwordBytes = Arrays.copyOf(Base64.getEncoder()
                    .encode(password.getBytes()), 16);
        }
    }

}
