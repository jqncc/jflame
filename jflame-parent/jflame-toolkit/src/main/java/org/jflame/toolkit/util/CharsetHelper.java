package org.jflame.toolkit.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符编码工具类.
 * 
 * @see java.nio.charset.StandardCharsets
 * @see org.apache.commons.lang3.CharUtils
 * @author yucan.zhang
 */
public class CharsetHelper {

    /**
     * gbk18030编码
     */
    public static final Charset GBK_18030 = Charset.forName("GB18030");
    /**
     * UTF-8编码名.<br>
     * 废除,请使用StandardCharsets.UTF_8
     */
    @Deprecated
    public static final String UTF_8 = "UTF-8";
    /**
     * UTF-16编码名.<br>
     * 废除,请使用StandardCharsets.UTF_16
     */
    @Deprecated
    public static final String UTF_16 = "UTF-16";

    /**
     * ISO-8859-1编码名 .<br>
     * 废除,请使用StandardCharsets.ISO_8859_1
     */
    @Deprecated
    public static final String ISO_8859_1 = "ISO-8859-1";
    /**
     * gbk编码
     */
    public static final Charset GBK = Charset.forName("gbk");

    /**
     * 使用utf-8解码字符串
     * 
     * @param string
     * @return
     */
    public static byte[] getUtf8Bytes(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 将byte[]使用utf-8编码为字符串
     * 
     * @param bytes byte[]
     * @return
     */
    public static String getUtf8String(byte[] bytes) {
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定编码重新编码字符串
     * 
     * @param str 字符串
     * @param decodeCharset 原字符编码
     * @param encodeCharset 新字符编码
     * @return
     */
    public static String reEncode(final String str, Charset decodeCharset, Charset encodeCharset) {
        return new String(str.getBytes(decodeCharset), encodeCharset);
    }

    /**
     * 解码GBK汉字编码为ISO88591
     * 
     * @param str
     * @return
     */
    public static String reEncodeGBK(final String str) {
        return reEncode(str, GBK, StandardCharsets.ISO_8859_1);
    }

}
