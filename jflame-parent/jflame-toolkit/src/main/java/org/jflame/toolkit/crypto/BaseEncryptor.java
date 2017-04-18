package org.jflame.toolkit.crypto;

import org.jflame.toolkit.util.CharsetHelper;

/**
 * 加密抽象父类 {@link http://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html}
 * <p>
 * jdk加密算法块模式和填充方式说明：
 * <ul>
 * <li>AES, ECB/CBC/PCBC/CTR/CTS/CFB/CFB8/CFB128/OFB/OFB8/OFB128, NOPADDING/PKCS5PADDING/ISO10126PADDING</li>
 * <li>DES, ECB/CBC/PCBC/CTR/CTS/CFB/CFB8/CFB64/OFB/OFB8/OFB64, NOPADDING/PKCS5PADDING/ISO10126PADDING</li>
 * <li>RSA, ECB, NOPADDING/PKCS1PADDING/OAEPWITHMD5ANDMGF1PADDING...</li>
 * <li>PBEWithMD5AndDES,CBC,PKCS5Padding</li>
 * </ul>
 * 
 * @author zyc
 */
public abstract class BaseEncryptor {


    /**
     * 加密算法名称枚举.DES, DESede, AES, RSA
     * 
     * @author zyc
     */
    public enum Algorithm {
        DES, DESede, AES, RSA
    }

    /**
     * 块加密模式枚举：ECB, CBC, CFB, OFB,PCBC, NONE
     * 
     * @author zyc
     */
    public enum OpMode {
        ECB, CBC, CFB, OFB, PCBC, NONE
    }

    /**
     * 填充模式枚举. PKCS1PADDING,PKCS5Padding, NoPadding, ISO10126PADDING
     * 
     * @author zyc
     */
    public enum Padding {
        PKCS1PADDING, PKCS5Padding, NoPadding, ISO10126PADDING
    }

    protected Algorithm curAlgorithm;
    protected OpMode curOpMode;
    protected Padding curPadding;
    protected String curCipher;
    protected String charset;

    public Algorithm getAlgorithm() {
        return curAlgorithm;
    }

    protected void setAlgorithm(Algorithm curAlgorithm) {
        this.curAlgorithm = curAlgorithm;
    }

    public String getCharset() {
        return charset == null ? CharsetHelper.UTF_8 : charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    protected void setOpMode(OpMode curOpMode) {
        this.curOpMode = curOpMode;
    }

    protected void setPadding(Padding curPaddingMode) {
        this.curPadding = curPaddingMode;
    }

    protected String getCipherStr() {
        String tram = curAlgorithm.name();
        if (curOpMode != null && curPadding != null) {
            tram = tram + '/' + curOpMode.name() + '/' + curPadding.name();
        }
        return tram;
    }
    
}
