package org.jflame.toolkit.codec;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jflame.toolkit.util.CharsetHelper;

/**
 * 转码工具类,支持base64,hex转码,int与byte[4]转换,url编码,byte与Long转换
 * 
 * @author zyc
 */
public final class TranscodeHelper {

    /**
     * 使用Base64编码字节数组,返回字符串
     * 
     * @param base64Data base64 byte[]
     * @return base64字符串
     */
    public static String encodeBase64String(byte[] base64Data) {
        return java.util.Base64.getEncoder()
                .encodeToString(base64Data);
    }

    /**
     * 使用base64编码字符串，指定字符编码
     * 
     * @param str base64字符串
     * @param charset 字符编码
     * @return base64字符串
     */
    public static String encodeBase64String(String str, Charset charset) {
        byte[] bytes = str.getBytes(charset);
        return encodeBase64String(bytes);
    }

    /**
     * 使用base64编码字符串,字符编码为utf-8
     * 
     * @param str 字符串
     * @return
     */
    public static String encodeBase64String(String str) {
        return encodeBase64String(str, StandardCharsets.UTF_8);
    }

    /**
     * 使用Base64编码字节数组,返回字节数组
     * 
     * @param base64Data base64 byte[]
     * @return byte[]
     */
    public static byte[] encodeBase64(byte[] base64Data) {
        return java.util.Base64.getEncoder()
                .encode(base64Data);
    }

    public static byte[] encodeBase64(String str) {
        return java.util.Base64.getEncoder()
                .encode(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码base64字符串,字符串默认使用utf-8解码
     * 
     * @param base64String base64字符串
     * @return
     */
    public static byte[] dencodeBase64(String base64String) {
        return java.util.Base64.getDecoder()
                .decode(base64String);
    }

    /**
     * 解码base64字节数组,返回字节数组
     * 
     * @param base64Data base64 byte[]
     * @return
     */
    public static byte[] dencodeBase64(byte[] base64Data) {
        return java.util.Base64.getDecoder()
                .decode(base64Data);
    }

    /**
     * 转码为16进制字符串
     * 
     * @param strBytes string byte[]
     * @return 16进制字符串
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
        return Hex.encodeHexString(CharsetHelper.getUtf8Bytes(str));
    }

    /**
     * 将16进制字符串解码为普通字节数组
     * 
     * @param hexString 16进制字符串
     * @return byte[]
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
     * @return utf-8编码字符串
     */
    public static String dencodeHexString(String hexString) throws TranscodeException {
        byte[] bytes = dencodeHex(hexString);
        return CharsetHelper.getUtf8String(bytes);
    }

    /**
     * utf-8 urlencode
     * 
     * @param str string
     * @return urlencode string
     */
    public static String urlencode(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    /**
     * utf-8 url decode
     * 
     * @param str url编码库
     * @return
     */
    public static String urldecode(String str) {
        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    /**
     * 转换int到4位byte数组
     * 
     * @param res int
     * @return byte[]
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
     * @param bytes 4位byte数组
     * @return int
     */
    public static int bytesToInt(byte[] bytes) {
        int value;
        value = (int) ((bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16)
                | ((bytes[3] & 0xFF) << 24));
        return value;
    }

    /**
     * 8位byte数组转long
     * 
     * @param bytes 8位byte数组
     * @return long
     */
    public static long bytesToLong(byte[] bytes) {
        long num = 0;
        for (int i = 0; i < 8; ++i) {
            num <<= 8;
            num |= (bytes[i] & 0xff);
        }
        return num;
    }

    /**
     * long转byte数组
     * 
     * @param num long
     * @return byte数组
     */
    public static byte[] longToBytes(long num) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (num >>> (56 - i * 8));
        }
        return b;
    }
}
