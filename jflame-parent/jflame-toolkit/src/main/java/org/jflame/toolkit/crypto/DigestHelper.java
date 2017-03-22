package org.jflame.toolkit.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.jflame.toolkit.codec.Hex;
import org.jflame.toolkit.codec.TranscodeHelper;

/**
 * 消息摘要加密算法工具类，支持md5/SHA-1/SHA-256/SHA512/HmacMD5.
 * 
 * @author zyc
 */
public class DigestHelper {

    private static final int STREAM_BUFFER_LENGTH = 1024;

    /**
     * 返回一个指定算法名的消息摘要对象
     * 
     * @param algorithm 算法名.具体请看
     *            <a href="http://java.sun.com/j2se/1.3/docs/guide/security/CryptoSpec.html#AppA">Appendix A in the Java
     *            Cryptography Architecture API Specification & Reference</a>
     * @throws RuntimeException 算法名不支持时异常 {@link java.security.NoSuchAlgorithmException}
     * @return
     */
    public static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * SHA-1算法计算字符串消息摘要.返回16进制字符串
     * 
     * @param plainText 明文字符串
     * @return 16进制字符串,长度40
     */
    public static String shaHex(String plainText) {
        return Hex.encodeHexString(sha(TranscodeHelper.getUtf8Bytes(plainText)));
    }

    /**
     * SHA-1算法计算byte[]消息摘要.返回byte[]
     * 
     * @param plainBytes byte[]
     * @return 摘要byte[]
     */
    public static byte[] sha(byte[] plainBytes) {
        return getDigest("SHA").digest(plainBytes);
    }

    /**
     * SHA-256算法计算字符串消息摘要.返回16进制字符串
     * 
     * @param plainText 明文字符串
     * @return 16进制字符串
     */
    public static String sha256Hex(String plainText) {
        return Hex.encodeHexString(sha256(TranscodeHelper.getUtf8Bytes(plainText)));
    }

    /**
     * SHA-256算法计算byte[]消息摘要.返回byte[]
     * 
     * @param plainBytes byte[]
     * @return 摘要byte[]
     */
    public static byte[] sha256(byte[] plainBytes) {
        return getDigest("SHA-256").digest(plainBytes);
    }

    /**
     * SHA-512算法计算字符串消息摘要.返回16进制字符串
     * 
     * @param plainText 明文字符串
     * @return 密文16进制字符串
     */
    public static String sha512Hex(String plainText) {
        return Hex.encodeHexString(sha512(TranscodeHelper.getUtf8Bytes(plainText)));
    }

    /**
     * SHA-256算法计算byte[]消息摘要.返回byte[]
     * 
     * @param plainBytes byte[]
     * @return 摘要byte[]
     */
    public static byte[] sha512(byte[] plainBytes) {
        return getDigest("SHA-512").digest(plainBytes);
    }

    /**
     * MD5算法计算byte[]消息摘要，返回16位 <code>byte[]</code>.
     * 
     * @param data byte[]待计算数据
     * @return MD5 16位byte[]
     */
    public static byte[] md5(byte[] data) {
        return getDigest("MD5").digest(data);
    }

    /**
     * MD5算法计算字符串消息摘要，返回16位 <code>byte[]</code>.字符串以utf-8编码
     * 
     * @param data String待计算字符串
     * @return MD5 16位byte[]
     */
    public static byte[] md5(String data) {
        return md5(TranscodeHelper.getUtf8Bytes(data));
    }

    /**
     * MD5算法计算输入流消息摘要
     * 
     * @param data InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] md5(InputStream data) throws IOException {
        return digest(getDigest("MD5"), data);
    }

    /**
     * MD5算法计算字符串消息摘要，返回32位十六制字符串
     * 
     * @param data 字符串
     * @return MD5 32位十六制字符串
     */
    public static String md5Hex(String data) {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * MD5算法计算byte[]消息摘要，返回32位十六制字符串
     * 
     * @param data byte[]待计算数据
     * @return 32位十六制字符串
     */
    public static String md5Hex(byte[] data) {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * md5加密字符串,返回16位长度.
     * 
     * @param data 字符串
     * @return 16进制字符串
     */
    public static String md5_16(String data) {
        return md5Hex(data).substring(8, 24);
    }

    /**
     * HmacMD5加密字节数组,指定密钥.
     * 
     * @param plainBytes 明文
     * @param keyBytes 密钥
     * @return 密文字节数组
     * @throws EncryptException 加密异常
     */
    public static byte[] hmacMD5(byte[] plainBytes, byte[] keyBytes) throws EncryptException {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacMD5");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            // 执行消息摘要 data是摘要后的结果
            byte[] data = mac.doFinal(plainBytes);
            return data;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new EncryptException(e);
        }
    }

    /**
     * HmacMD5加密字符串.
     * 
     * @param plainText 明文
     * @param key 密钥
     * @return 密文 16进制字符串
     * @throws EncryptException 加密异常
     */
    public static String hmacMD5Hex(String plainText, String key) throws EncryptException {
        byte[] macData = hmacMD5(TranscodeHelper.getUtf8Bytes(plainText), TranscodeHelper.getUtf8Bytes(key));
        return TranscodeHelper.encodeHexString(macData);
    }

    private static byte[] digest(MessageDigest digest, InputStream data) throws IOException {
        byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        int read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);

        while (read > -1) {
            digest.update(buffer, 0, read);
            read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        return digest.digest();
    }

    /*
     * public static void main(String[] args) { System.out.println("hmacMD5:"+hmacMD5Hex("中国人", "123"));
     * System.out.println(md5("中国人")); System.out.println(sha("中国人")); }
     */
}
