package org.jflame.commons.config;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.crypto.SymmetricEncryptor;
import org.jflame.commons.crypto.BaseEncryptor.Algorithm;
import org.jflame.commons.crypto.BaseEncryptor.OpMode;
import org.jflame.commons.crypto.BaseEncryptor.Padding;
import org.jflame.commons.util.CharsetHelper;

/**
 * 支持加密属性值的属性文件加载,内置加密aes/ecb/pkcs5padding
 * 
 * @author yucan.zhang
 */
public final class EncryptPropertiesConfig extends PropertiesConfig {

    private SymmetricEncryptor aes = new SymmetricEncryptor(Algorithm.AES, OpMode.ECB, Padding.PKCS5Padding);
    private String[] encryptKeys;
    private byte[] password;// 密钥,16位

    @Override
    protected Object convertProperty(String propertyName, Object propertyValue) {
        if (propertyValue != null && ArrayUtils.contains(encryptKeys, propertyName)) {
            return aes.dencryptHex(propertyValue.toString(), password, null);
        } else {
            return propertyValue;
        }
    }

    /**
     * 被加密的属性名
     * 
     * @param encryptKeys
     */
    public void setEncryptKeys(String[] encryptKeys) {
        this.encryptKeys = encryptKeys;
    }

    @Override
    public void setValueConvert(boolean valueConvert) {
        super.setValueConvert(true);
    }

    public void setPassword(String password) {
        byte[] pwdBytes = CharsetHelper.getUtf8Bytes(password);
        if (pwdBytes.length < 16) {
            throw new IllegalArgumentException("密钥必须是大于16 byte");
        } else if (pwdBytes.length == 16) {
            this.password = pwdBytes;
        } else {
            this.password = Arrays.copyOf(pwdBytes, 16);
        }
    }

}
