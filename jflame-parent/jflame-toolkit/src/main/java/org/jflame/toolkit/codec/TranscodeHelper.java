package org.jflame.toolkit.codec;

import java.nio.charset.Charset;

import org.jflame.toolkit.util.StringHelper;

/**
 * 转码工具类,支持base64,hex转码,int与byte[4]转换
 * 
 * @author zyc
 */
public final class TranscodeHelper {

    /**
     * 使用Base64编码字节数组,返回字符串
     * 
     * @param base64Data
     * @return 返回字符串
     */
    public static String encodeBase64String(byte[] base64Data) {
        return Base64.encodeBase64String(base64Data);
    }

    /**
     * 使用base64编码字符串，指定字符编码
     * 
     * @param str
     * @param charset 字符编码，为null默认使用utf-8
     * @return
     */
    public static String encodeBase64String(String str, Charset charset) {
        byte[] bytes = null;
        if (charset == null) {
            bytes = StringHelper.getUtf8Bytes(str);
        } else {
            bytes = str.getBytes(charset);
        }
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 使用Base64编码字节数组,返回字节数组
     * 
     * @param base64Data
     * @return
     */
    public static byte[] encodeBase64(byte[] base64Data) {
        return Base64.encodeBase64(base64Data);
    }

    /**
     * 解码base64字符串,字符串默认使用utf-8解码
     * 
     * @param base64String
     * @return
     */
    public static byte[] dencodeBase64(String base64String) {
        return Base64.decodeBase64(base64String);
    }

    /**
     * 解码base64字节数组,返回字节数组
     * 
     * @param base64Data
     * @return
     */
    public static byte[] dencodeBase64(byte[] base64Data) {
        return Base64.decodeBase64(base64Data);
    }

    /**
     * 转码为16进制字符串
     * 
     * @param strBytes
     * @return
     */
    public static String encodeHexString(byte[] strBytes) {
        return Hex.encodeHexString(strBytes);
    }

    /**
     * 转码为16进制字符串，默认以utf-8解码字符str
     * 
     * @param str 待转码字符串
     * @return 16进制字符串
     */
    public static String encodeHexString(String str) {
        return Hex.encodeHexString(StringHelper.getUtf8Bytes(str));
    }

    /**
     * 将16进制字符串解码为普通字节数组
     * 
     * @param hexString 16进制字符串
     * @return
     * @throws TranscodeException
     */
    public static byte[] dencodeHex(String hexString) throws TranscodeException {
        if (hexString == null) {
            return null;
        }
        return Hex.decodeHex(hexString.toCharArray());
    }

    /**
     * 将16进制字符串解码为普通字符串，默认以utf-8编码
     * 
     * @param hexString 16进制字符串
     * @return
     */
    public static String dencodeHexString(String hexString) throws TranscodeException {
        byte[] bytes = dencodeHex(hexString);
        return StringHelper.getUtf8String(bytes);
    }

    /**
     * 转换int到4位byte数组
     * 
     * @param res
     * @return
     */
    public static byte[] intTo4Bytes(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    /**
     * 4位byte数组转int
     * 
     * @param bytes
     * @return
     */
    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    /**
     * 8位byte数组转long
     * 
     * @param bytes
     * @return
     */
    public static long bytesToLong(byte[] bytes) {
        long num = 0;
        for (int i = 0; i < 8; ++i) {
            num <<= 8;
            num |= (bytes[i] & 0xff);
        }
        return num;
    }

}
