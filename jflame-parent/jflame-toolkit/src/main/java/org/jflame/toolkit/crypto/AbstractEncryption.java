package org.jflame.toolkit.crypto;

import javax.crypto.Cipher;

/**
 * 加密抽象父类
 * {@link http://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html}
 * 
 * @author zyc
 */
public abstract class AbstractEncryption {
    /**
     * 计算方式枚举，加密或解密.
     */
    public enum CipherMode {
        ENCRYPT(Cipher.ENCRYPT_MODE), DENCRYPT(Cipher.DECRYPT_MODE);
        private CipherMode(int value) {
            this.value = value;
        }

        private int value;

        public int getValue() {
            return this.value;
        }
    }

    /**
     * 加密算法名称枚举.
     * 
     * @author zyc
     */
    public enum Algorithm {
        DES, DESede, AES, RSA, PBEWITHMD5ANDDES
    }

    /**
     * 加密模式,none适合于rsa.
     * 
     * @author zyc
     */
    public enum EncryptMode {
        ECB, CBC, CFB, OFB, NONE
    }

    /**
     * 填充模式枚举.
     * 
     * @author zyc
     */
    public enum PaddingMode {
        PKCS5Padding, PKCS7Padding, NoPadding, PKCS1Padding, OAEPWithSHA1AndMGF1Padding
    }

    protected Algorithm curAlgorithm;
    protected EncryptMode curEncryptMode;
    protected PaddingMode curPaddingMode;
    protected String curCipher;
    protected String charset = "utf-8";

    public Algorithm getCurAlgorithm() {
        return curAlgorithm;
    }

    public void setCurAlgorithm(Algorithm curAlgorithm) {
        this.curAlgorithm = curAlgorithm;
    }

    public EncryptMode getCurEncryptMode() {
        return curEncryptMode;
    }

    public void setCurEncryptMode(EncryptMode curEncryptMode) {
        this.curEncryptMode = curEncryptMode;
    }

    public PaddingMode getCurPaddingMode() {
        return curPaddingMode;
    }

    public void setCurPaddingMode(PaddingMode curPaddingMode) {
        this.curPaddingMode = curPaddingMode;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCurCipher() {
        return curAlgorithm.name() + '/' + curEncryptMode.name() + '/' + curPaddingMode;
    }

    /*
     * AES/CBC/NoPadding (128) AES/CBC/PKCS5Padding (128) AES/ECB/NoPadding (128)
     * AES/ECB/PKCS5Padding (128) DES/CBC/NoPadding (56) DES/CBC/PKCS5Padding (56) DES/ECB/NoPadding
     * (56) DES/ECB/PKCS5Padding (56) DESede/CBC/NoPadding (168) DESede/CBC/PKCS5Padding (168)
     * DESede/ECB/NoPadding (168) DESede/ECB/PKCS5Padding (168) RSA/ECB/PKCS1Padding (1024, 2048)
     * RSA/ECB/OAEPWithSHA-1AndMGF1Padding (1024, 2048) RSA/ECB/OAEPWithSHA-256AndMGF1Padding (1024,
     * 2048)
     */

}
