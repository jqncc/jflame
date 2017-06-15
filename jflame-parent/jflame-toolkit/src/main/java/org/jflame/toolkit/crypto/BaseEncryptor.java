package org.jflame.toolkit.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.StringHelper;

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

    protected String providerName;
    protected Algorithm curAlgorithm;
    protected OpMode curOpMode;
    protected Padding curPadding;
    protected String curCipherStr;
    protected String charset;
    protected Cipher cipher;
    
    /**
     * 构造函数,指定加密算法,填充模式.
     * 
     * @param algorithm 算法名
     * @param encMode 加密方式
     * @param paddingMode 填充方式
     */
    public BaseEncryptor(Algorithm algorithm, OpMode encMode, Padding paddingMode) {
        curAlgorithm = algorithm;
        curOpMode = encMode;
        curPadding = paddingMode;
        initCipher();
    }
    
    /**
     * 构造函数,指定加密算法,填充模式.
     * @param algorithm 算法名
     * @param encMode 加密方式
     * @param paddingMode 填充方式
     * @param provider 加密提供器,如:BouncyCastleProvider 
     * @param providerName 加密提供器名称 ,如:BouncyCastleProvider的叫"BC"
     */
    public BaseEncryptor(Algorithm algorithm, OpMode encMode, Padding paddingMode, Provider provider,
            String providerName) {
        curAlgorithm = algorithm;
        curOpMode = encMode;
        curPadding = paddingMode;
        if (provider != null) {
            Security.addProvider(provider);
            if (StringHelper.isEmpty(providerName)) {
                throw new IllegalArgumentException("参数providerName不能为null");
            }
            this.providerName = providerName;
        }
        initCipher();
    }

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
     * 填充模式枚举. PKCS1PADDING(RSA),PKCS5Padding, NoPadding, ISO10126PADDING,OAEPWITHMD5ANDMGF1PADDING(RSA)
     * 
     * @author zyc
     */
    public enum Padding {
        PKCS1PADDING, PKCS5Padding,PKCS7Padding, NoPadding, ISO10126PADDING, OAEPWITHMD5ANDMGF1PADDING
    }

    public Algorithm getAlgorithm() {
        return curAlgorithm;
    }

    /*
     * protected void setAlgorithm(Algorithm curAlgorithm) { this.curAlgorithm = curAlgorithm; }
     */

    public String getCharset() {
        return charset == null ? CharsetHelper.UTF_8 : charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    /*
     * protected void setOpMode(OpMode curOpMode) { this.curOpMode = curOpMode; } protected void setPadding(Padding
     * curPaddingMode) { this.curPadding = curPaddingMode; }
     */

    protected String getCipherStr() {
        String tram = curAlgorithm.name();
        if (curOpMode != null && curPadding != null) {
            tram = tram + '/' + curOpMode.name() + '/' + curPadding.name();
        }
        return tram;
    }

    protected void initCipher() throws EncryptException {
        if (!isSupport()) {
            throw new EncryptException("不支持的加密算法" + curAlgorithm.name());
        }
        curCipherStr = getCipherStr();
        try {
            if (StringHelper.isNotEmpty(providerName)) {
                cipher = Cipher.getInstance(curCipherStr, providerName);
            } else {
                cipher = Cipher.getInstance(curCipherStr);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new EncryptException("初始加密算法失败" + curCipherStr, e);
        } catch (NoSuchProviderException e) {
            throw new EncryptException("未找到名为" + providerName + "的Provider", e);
        }
    }

    /**
     * 是否支持的算法
     */
    public abstract boolean isSupport();
}
