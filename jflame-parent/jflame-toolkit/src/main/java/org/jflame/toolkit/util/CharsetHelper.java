package org.jflame.toolkit.util;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

/**
 * 字符编码工具类.
 * 
 * @author yucan.zhang
 */
public class CharsetHelper {
    
    /**
     * gbk编码名
     */
    public static final String GBK = "GBK";
    /**
     * gbk18030编码名
     */
    public static final String GBK_18030 = "GB18030";
    /**
     * utf-8编码名
     */
    public static final String UTF_8 = "UTF-8";
    /**
     * utf-6编码名
     */
    public static final String UTF_16 = "UTF-16";
    /**
     * Big5编码名
     */
    public static final String BIG_5 = "Big5";
    /**
     * ISO-8859-1编码名
     */
    public static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * 是否支持的字符集
     * 
     * @param charsetName 字符集名称
     * @return
     */
    public static boolean isSupported(String charsetName) {
        if (charsetName == null) {
            return false;
        }
        try {
            return Charset.isSupported(charsetName);
        } catch (IllegalCharsetNameException ex) {
            return false;
        }
    }

}
