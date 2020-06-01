package org.jflame.commons.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.model.Chars;
import org.jflame.commons.util.StringHelper;

/**
 * Hex 十六进制编码工具类.
 * 
 * @author yucan.zhang
 */
public final class Hex {

    /**
     * 二进制数组编码为16进制字符数组
     * 
     * @param data 待编码的二进制数组
     * @return 返回16进制字符数组
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, Chars.HEX_CHARS);
    }

    /**
     * 将二进制数组编码为16进制字符串
     * 
     * @param data 待编码的二进制数组byte[]
     * @return 返回16进制字符串
     */
    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    /**
     * 将字符串编码16进制字符串,默认使用utf-8解码字符串
     * 
     * @param text 待编码字符串
     * @return
     */
    public static String encodeHexString(String text) {
        return encodeHexString(text, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串编码16进制字符串,使用指定字符集解码字符串
     * 
     * @param text 待编码字符串
     * @param charset 字符串编码
     * @return
     */
    public static String encodeHexString(String text, Charset charset) {
        if (StringHelper.isEmpty(text)) {
            return text;
        }
        byte[] bytes = text.getBytes(charset);
        return encodeHexString(bytes);
    }

    static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    static int toDigit(char ch, int index) throws ConvertException {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new ConvertException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    /**
     * 解码16进制字符数组.
     * 
     * @param data 16进制字符数组
     * @return 解码后的二进制数组
     * @throws ConvertException
     */
    public static byte[] decodeHex(char[] data) throws ConvertException {
        int len = data.length;
        if ((len & 0x01) != 0) {
            throw new ConvertException("数组长度应该是偶数");
        }

        byte[] out = new byte[len >> 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 解码6进制字符串,再用utf-8编码字符串
     * 
     * @param hexText 16进制字符
     * @return 返回utf-8编码后的字符串
     * @throws ConvertException
     */
    public static String decodeHex(String hexText) throws ConvertException {
        byte[] bytes = decodeHex(hexText.toCharArray());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 解码6进制字符串,再用指定字符集编码字符串
     * 
     * @param hexText 16进制字符
     * @param charset
     * @return
     * @throws ConvertException
     */
    public static String decodeHex(String hexText, Charset charset) throws ConvertException {
        byte[] bytes = decodeHex(hexText.toCharArray());
        return new String(bytes, charset);
    }

}
