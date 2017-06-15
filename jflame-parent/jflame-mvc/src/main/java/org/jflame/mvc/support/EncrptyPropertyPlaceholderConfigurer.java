package org.jflame.mvc.support;

import org.apache.commons.lang3.ArrayUtils;
import org.jflame.toolkit.crypto.BaseEncryptor.Algorithm;
import org.jflame.toolkit.crypto.SymmetricEncryptor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 支持加密属性的资源文件
 * 
 * @author yucan.zhang
 */
public class EncrptyPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private String[] encrptyKeys;
    private SymmetricEncryptor aes = new SymmetricEncryptor(Algorithm.AES);
    private final byte[] passwd = new byte[]{ 0x2,0x9,0x5,0x6,0x8,0x2,0x2,0x1,0x3,0x4,0x6,0x5,0x2,0x9,0x0,0x6 };

    @Override
    protected String convertProperty(String propertyName, String propertyValue) {
        if (ArrayUtils.contains(encrptyKeys, propertyName)) {
            return aes.dencryptHex(propertyValue, passwd, null);
        } else {
            return propertyValue;
        }
    }

    /**
     * 加密的属性名
     * 
     * @param encrptyKeys
     */
    public void setEncrptyKeys(String[] encrptyKeys) {
        this.encrptyKeys = encrptyKeys;
    }

}
