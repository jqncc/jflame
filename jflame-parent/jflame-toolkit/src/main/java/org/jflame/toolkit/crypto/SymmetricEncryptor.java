package org.jflame.toolkit.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.Provider;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jflame.toolkit.codec.Hex;
import org.jflame.toolkit.codec.TranscodeException;
import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * 对称加密,支持算法des,3des,aes.
 * <p>
 * 字符串默认utf-8编码.<br>
 * <strong>AES说明:</strong>
 * <ol>
 * <li><strong>密钥16byte，向量IV 16byte(ECB无需向量)</strong>，jdk默认只支持128bit密钥长度(使用256bit需下载JCE扩展包替换 )</li>
 * <li>支持填充方式PKCS5Padding/NoPadding/ISO10126PADDING,不设置填充模式jdk1.7默认AES/ECB/PKCS5Padding</li>
 * <li>使用PKCS7Padding实际是PKCS5Padding</li>
 * <li>使用NoPadding填充，请确保明文是16的倍数</li>
 * </ol>
 * <strong>3DES说明:</strong>
 * <ol>
 * <li><strong>密钥24byte，向量IV 8byte(ECB无需向量)</strong></li>
 * <li>支持填充方式PKCS5Padding/NoPadding/ISO10126PADDING,不设置填充模式jdk1.7默认DESdede/ECB/PKCS5Padding</li>
 * <li>使用NoPadding填充，请确保明文是8的倍数</li>
 * </ol>
 * * <strong>DES说明:</strong>
 * <ol>
 * <li><strong>密钥8byte，向量IV 8byte(ECB无需向量)</strong></li>
 * <li>支持填充方式PKCS5Padding/NoPadding/ISO10126PADDING,不设置填充模式jdk1.7默认DESdede/ECB/PKCS5Padding</li>
 * <li>使用NoPadding填充，请确保明文是8的倍数</li>
 * </ol>
 * 
 * @author zyc
 */
public class SymmetricEncryptor extends BaseEncryptor {

    /**
     * 构造函数,使用默认加密填充方式ECB/PKCS5Padding.
     * 
     * @param algorithm 算法名称
     */
    public SymmetricEncryptor(Algorithm algorithm) {
        super(algorithm, OpMode.ECB, Padding.PKCS5Padding);
    }

    /**
     * 构造函数,指定加密算法,填充模式.
     * 
     * @param algorithm 算法名
     * @param encMode 加密方式
     * @param paddingMode 填充方式
     */
    public SymmetricEncryptor(Algorithm algorithm, OpMode encMode, Padding paddingMode) {
        super(algorithm, encMode, paddingMode);
    }

    /**
     * 构造函数,使用默认加密填充方式ECB/PKCS5Padding,同时指定加密提供器provider
     * 
     * @param algorithm 算法名称
     * @param provider 加密提供器,如:BouncyCastleProvider
     * @param providerName 加密提供器名称 ,如:BouncyCastleProvider的叫"BC"
     */
    public SymmetricEncryptor(Algorithm algorithm, Provider provider, String providerName) {
        super(algorithm, OpMode.ECB, Padding.PKCS5Padding, provider, providerName);
    }

    /**
     * 构造函数,指定加密算法,填充模式,同时指定加密提供器provider
     * 
     * @param algorithm 算法名
     * @param encMode 加密方式
     * @param paddingMode 填充方式
     * @param provider 加密提供器,如:BouncyCastleProvider
     * @param providerName 加密提供器名称 ,如:BouncyCastleProvider的叫"BC"
     */
    public SymmetricEncryptor(Algorithm algorithm, OpMode encMode, Padding paddingMode, Provider provider,
            String providerName) {
        super(algorithm, encMode, paddingMode, provider, providerName);
    }

    /**
     * 加密.
     * 
     * @param data 明文
     * @param keybytes 密钥
     * @param ivParam 向量,无需向量传null
     * @return 密文byte[]
     * @throws EncryptException 加密异常
     */
    public byte[] encrypt(final byte[] data, final byte[] keybytes, byte[] ivParam) throws EncryptException {
        return doCompute(data, keybytes, ivParam, Cipher.ENCRYPT_MODE);
    }

    /**
     * 解密.
     * 
     * @param data byte[]密文
     * @param keybytes byte[]密钥
     * @param ivParam byte[]向量
     * @return byte[]明文
     * @throws EncryptException 解密异常
     */
    public byte[] decrypt(final byte[] data, final byte[] keybytes, byte[] ivParam) throws EncryptException {
        return doCompute(data, keybytes, ivParam, Cipher.DECRYPT_MODE);
    }

    /**
     * 加密字符串,返回base64密文.
     * 
     * @param plainText 明文
     * @param password 密钥
     * @param ivParam 向量 ,无需向量传null
     * @return 密文,base64字符串
     * @throws EncryptException 编码转换或加密异常
     */
    public String encrytTextToBase64(final String plainText, final byte[] password, final byte[] ivParam)
            throws EncryptException {
        if (StringHelper.isEmpty(plainText)) {
            return plainText;
        }
        byte[] ciphertext;
        try {
            ciphertext = encrypt(plainText.getBytes(getCharset()), password, ivParam);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }

        return isEnableBase64UrlSafe() ? Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(ciphertext)
                : Base64.getEncoder()
                        .encodeToString(ciphertext);
    }

    /**
     * 加密字符串,返回十六进制密文.
     * 
     * @param plainText 明文
     * @param password 密钥
     * @param ivParam 向量,无需向量传null
     * @return 密文,十六进制字符串
     * @throws EncryptException 编码转换或加密异常
     */
    public String encrytTextToHex(final String plainText, final byte[] password, final byte[] ivParam)
            throws EncryptException {
        if (StringHelper.isEmpty(plainText)) {
            return plainText;
        }
        byte[] ciphertext;
        try {
            ciphertext = encrypt(plainText.getBytes(getCharset()), password, ivParam);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }
        return TranscodeHelper.encodeHexString(ciphertext);
    }

    /**
     * 解密字符串,密文为base64编码.
     * 
     * @param cipherBase64Text 密文
     * @param password 密钥
     * @param ivParam 向量
     * @return 解密后字符串
     * @throws EncryptException 编码转换或解密异常
     */
    public String decryptBase64(String cipherBase64Text, final byte[] password, final byte[] ivParam)
            throws EncryptException {
        if (StringHelper.isEmpty(cipherBase64Text)) {
            return cipherBase64Text;
        }
        byte[] cipherBytes = Base64.getDecoder()
                .decode(cipherBase64Text);
        return doDencrypt(cipherBytes, password, ivParam);
    }

    /**
     * 解密字符串,密文为16进制编码.
     * 
     * @param cipherHexText 密文
     * @param password 密钥
     * @param ivParam 向量
     * @return 解密后字符串
     * @throws EncryptException 字符编码转换或解密异常
     */
    public String dencryptHex(String cipherHexText, final byte[] password, final byte[] ivParam)
            throws EncryptException {
        if (StringHelper.isEmpty(cipherHexText)) {
            return cipherHexText;
        }
        byte[] cipherBytes = null;
        try {
            cipherBytes = Hex.decodeHex(cipherHexText.toCharArray());
        } catch (TranscodeException e) {
            throw new EncryptException("解密异常", e);
        }
        return doDencrypt(cipherBytes, password, ivParam);
    }

    private String doDencrypt(final byte[] cipherBytes, final byte[] password, final byte[] ivParam)
            throws EncryptException {
        byte[] plainBytes = decrypt(cipherBytes, password, ivParam);
        try {
            return new String(plainBytes, getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 执行加解密算法计算
     * 
     * @param data byte[] 内容
     * @param keybytes 密钥
     * @param ivParam 向量
     * @param cipherMode 计算方式,即加密或解密，取值:Cipher.ENCRYPT_MODE,Cipher.DECRYPT_MODE
     * @return
     * @throws EncryptException
     */
    private byte[] doCompute(final byte[] data, final byte[] keybytes, byte[] ivParam, int cipherMode)
            throws EncryptException {
        if (data == null) {
            return null;
        }
        Key key;
        try {
            key = new SecretKeySpec(keybytes, curAlgorithm.name());
            if (curOpMode == null || curOpMode == OpMode.ECB) {
                cipher.init(cipherMode, key);
            } else {
                cipher.init(cipherMode, key, new IvParameterSpec(ivParam));
            }
            byte[] cipherText = cipher.doFinal(data);
            return cipherText;
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new EncryptException(cipherMode == Cipher.ENCRYPT_MODE ? "加密异常" : "解密异常", e);
        }
    }

    @Override
    public boolean isSupport() {
        if (curAlgorithm != Algorithm.AES && curAlgorithm != Algorithm.DES && curAlgorithm != Algorithm.DESede) {
            return false;
        }
        return true;
    }

    /**
     * aes/ecb/pck5padding加密文本
     * 
     * @param plainText 明文
     * @param passwdBytes 密钥
     * @return base64密文
     * @throws EncryptException 加密失败
     */
    public static String aesEncrypt(String plainText, byte[] passwdBytes) throws EncryptException {
        SymmetricEncryptor aes = new SymmetricEncryptor(Algorithm.AES);
        return aes.encrytTextToBase64(plainText, passwdBytes, null);
    }

    /**
     * 指定加密和填充模式aes加密
     * 
     * @param encMode 加密方式
     * @param paddingMode 填充模式
     * @param plainText 明文
     * @param passwdBytes 密钥
     * @param ivBytes 向量
     * @return base64密文
     * @throws EncryptException 加密失败
     */
    public static String aesEncrypt(OpMode encMode, Padding paddingMode, String plainText, byte[] passwdBytes,
            byte[] ivBytes) throws EncryptException {
        if (encMode == null || paddingMode == null) {
            throw new IllegalArgumentException("参数encMode,paddingMode不能为空");
        }
        SymmetricEncryptor aes = new SymmetricEncryptor(Algorithm.AES, encMode, paddingMode);
        return aes.encrytTextToBase64(plainText, passwdBytes, ivBytes);
    }

    /**
     * aes/ecb/pck5padding解密base64密文
     * 
     * @param cipherBase64text base64密文
     * @param passwdBytes 密钥
     * @return 明文
     * @throws EncryptException 解密失败
     */
    public static String aesDencrypt(String cipherBase64text, byte[] passwdBytes) throws EncryptException {
        SymmetricEncryptor aes = new SymmetricEncryptor(Algorithm.AES);
        return aes.decryptBase64(cipherBase64text, passwdBytes, null);
    }

    /**
     * 指定加密和填充模式aes解密base64密文
     * 
     * @param encMode 加密方式
     * @param paddingMode 填充模式
     * @param cipherBase64text base64密文
     * @param passwdBytes 密钥
     * @param ivBytes 向量
     * @return 明文
     * @throws EncryptException 解密失败
     */
    public static String aesDencrypt(OpMode encMode, Padding paddingMode, String cipherBase64text, byte[] passwdBytes,
            byte[] ivBytes) throws EncryptException {
        SymmetricEncryptor aes = new SymmetricEncryptor(Algorithm.AES, encMode, paddingMode);
        return aes.decryptBase64(cipherBase64text, passwdBytes, ivBytes);
    }
}
