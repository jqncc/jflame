package org.jflame.toolkit.crypto;

import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jflame.toolkit.codec.Base64;

/**
 * PBE加密算法,支持PBEWithMD5AndDES, PBEWithSHA1AndDESede.
 * 
 * @author zyc
 */
public class PBEEncryption extends AbstractEncryption {
    public enum PbeMode {
        PBEWithMD5AndDES, PBEWithSHA1AndDESede// PBEWith<digest>And<encryption>
    }

    private final int iteratCount = 20;
    private PbeMode curMode;

    public PBEEncryption() {
        curMode = PbeMode.PBEWithMD5AndDES;
    }

    public PBEEncryption(PbeMode curMode) {
        this.curMode = curMode;
        Security.addProvider(new BouncyCastleProvider());
    }

    private byte[] doCipher(byte[] content, String key, byte[] salt, CipherMode cipherMode) throws EncryptException {
        SecretKey secretKey = null;
        try {
            Cipher cipher = Cipher.getInstance(curMode.name());
            PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray());
            SecretKeyFactory factory = SecretKeyFactory.getInstance(curMode.name());
            PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, iteratCount);
            secretKey = factory.generateSecret(keySpec);
            cipher.init(cipherMode.getValue(), secretKey, parameterSpec);
            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new EncryptException(e);
        }
    }

    /**
     * pbe加密.
     * 
     * @param content 明文
     * @param password 密钥
     * @param salt 盐
     * @return 密文,base64字符串
     * @throws EncryptException 加解密异常
     */
    public String encryptBase64(String content, String password, byte[] salt) throws EncryptException {
        byte[] cipher;
        try {
            cipher = doCipher(content.getBytes(charset), password, salt, CipherMode.ENCRYPT);
            return Base64.encodeBase64String(cipher);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 解密.
     * 
     * @param cipherBase64 密文,base64字符串
     * @param password 密钥
     * @param salt 盐
     * @return 明文
     * @throws EncryptException 加解密异常
     */
    public String dencryptBase64(String cipherBase64, String password, byte[] salt) throws EncryptException {
        byte[] cipher;
        try {
            cipher = doCipher(Base64.decodeBase64(cipherBase64), password, salt, CipherMode.DENCRYPT);
            return new String(cipher, charset);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }
    }

    /**
     * 随机生成一个盐.
     * 
     * @return
     */
    byte[] randomSalt() {
        byte[] salt = new byte[8];
        Random rand = new Random();
        rand.nextBytes(salt);
        return salt;
    }

    /*
     * public static void main(String[] args) { PBEEncryption pbe = new
     * PBEEncryption(PBE_MODE.PBEWithMD5AndDES); byte[] salt = new byte[] { 0x7d, 0x60, 0x43, 0x5f,
     * 0x02, (byte) 0xe9, (byte) 0xe0, (byte) 0xae }; // byte[] salt = new byte[8]; salt[0] = 0x7d;
     * String content = "中国字加密"; String password = "321122jjjd"; String cipher =
     * pbe.encryptBase64(content, password, salt); System.out.println(cipher);
     * System.out.println(pbe.dencryptBase64(cipher, password, salt)); }
     */
}
