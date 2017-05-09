package org.jflame.toolkit.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.codec.TranscodeException;

/**
 * 字符串工具类
 * 
 * @see org.apache.commons.lang3.StringUtils
 * @see org.apache.commons.lang3.RandomStringUtils
 * @author zyc
 */
public final class StringHelper {

    /**
     * 判断字符串是否为空 即等于null or ""
     * 
     * @param str 字符串
     * @return true空，否则false
     */
    public static boolean isEmpty(CharSequence str) {
        return StringUtils.isEmpty(str);
    }

    /**
     * 判断字符串是否不为空 即不等于null or ""
     * 
     * @param str 字符串
     * @return
     */
    public static boolean isNotEmpty(CharSequence str) {
        return StringUtils.isNotEmpty(str);
    }
    
    /**
     * 检查字符串中是否包含给定的任意字符
     * @param str
     * @param searchChars
     * @return
     */
    public static boolean containsAny(CharSequence str, char... searchChars) {
        return StringUtils.containsAny(str, searchChars);
    }
    
    /**
     * 以指定分隔符组合数组元素为字符串,示例:<br>
     * StringHelper.join(["a", "b", "c"], ',') = "a,b,c"
     * 
     * @param array 数组
     * @param separator 分隔符
     * @return
     */
    public static String join(Object[] array, char separator) {
        return StringUtils.join(array, separator);
    }

    /**
     * 将url参数字符串转为Map 如:x=1&y=2转为 map.put("x","1").
     * 
     * @see #buildUrlParamFromMap(Map)
     * @param paramStr url参数字符串, 如:x=1&y=2
     * @return Map&lt;String, String&gt;
     */
    public static Map<String,String> buildMapFromUrlParam(String paramStr) {
        if (isEmpty(paramStr)) {
            return null;
        }
        Map<String,String> map = new HashMap<String,String>();
        final String[] splitChars = { "&","=","" };
        String[] tmpArr = paramStr.split(splitChars[0]);
        String[] kv;
        if (tmpArr != null) {
            for (String tmp : tmpArr) {
                kv = tmp.split(splitChars[1]);
                if (kv.length >= 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                } else {
                    map.put(kv[0].trim(), splitChars[2]);
                }
            }
        }
        return map;
    }

    /**
     * 将map转为url参数字符串 如:key=value&key1=value1
     * 
     * @param paramMap Map&lt;String, String&gt;
     * @see #buildMapFromUrlParam(String)
     * @return url参数字符串, 如:x=1&y=2
     */
    public static String buildUrlParamFromMap(Map<String,String> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }
        StringBuilder strBuf = new StringBuilder(20);
        for (Entry<String,String> kv : paramMap.entrySet()) {
            strBuf.append('&').append(kv.getKey()).append('=').append(kv.getValue());
        }
        strBuf.deleteCharAt(0);
        return strBuf.toString();
    }

    /**
     * 将驼峰命名的字符串转为下划线分隔形式
     * 
     * @param camelStr 驼峰命名的字符串
     * @return
     */
    public static String camelToUnderline(String camelStr) {
        int len = camelStr.length();
        final char UNDER_LINE = '_';
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = camelStr.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append(UNDER_LINE);
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线分隔形式转为驼峰命名字符串
     * 
     * @param underlineStr 下划线分隔形式的字符串
     * @return
     */
    public static String underlineToCamel(String underlineStr) {
        final char UNDER_LINE = '_';
        int len = underlineStr.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = underlineStr.charAt(i);
            if (c == UNDER_LINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(underlineStr.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 替换html文本中的特殊符号为html实体 如:<html></html> 转换后 "&lt;html&gt;&lt;/html&gt;
     * 
     * @param htmlText html文本
     * @see #unescapeHTML(String)
     * @see org.apache.commons.lang3.StringEscapeUtils
     * @return
     */
    public static String escapeHTML(String htmlText) {
        return StringEscapeUtils.escapeHtml4(htmlText);
    }

    /**
     * 将文本中的html实体符还原回html符号
     * 
     * @param escapeHtmlText html文本
     * @see #escapeHTML(String)
     * @see org.apache.commons.lang3.StringEscapeUtils
     * @return
     */
    public static String unescapeHTML(String escapeHtmlText) {
        return StringEscapeUtils.unescapeHtml4(escapeHtmlText);
    }

    /**
     * 删除文本中的html标签
     * 
     * @param htmlText html文本
     * @return
     */
    public static String removeHtmlTag(String htmlText) {
        if (htmlText == null) {
            return null;
        }
        String htmlStr = htmlText; // 含html标签的字符串
        // script正则{或<script[^>]*?>[\\s\\S]*?<\\/script>
        String scriptRegex = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
        // style正则{或<style[^>]*?>[\\s\\S]*?<\\/style>
        String styleRegex = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
        // HTML标签正则
        String htmlRegex = "<[^>]+>";

        // 过滤script标签
        Pattern scriptPattern = Pattern.compile(scriptRegex, Pattern.CASE_INSENSITIVE);
        Matcher scriptMatcher = scriptPattern.matcher(htmlStr);
        htmlStr = scriptMatcher.replaceAll("");
        // 过滤style标签
        Pattern stylePattern = Pattern.compile(styleRegex, Pattern.CASE_INSENSITIVE);
        Matcher styleMatcher = stylePattern.matcher(htmlStr);
        htmlStr = styleMatcher.replaceAll("");
        // 过滤html标签
        Pattern htmlPattern = Pattern.compile(htmlRegex, Pattern.CASE_INSENSITIVE);
        Matcher htmlMatcher = htmlPattern.matcher(htmlStr);
        htmlStr = htmlMatcher.replaceAll("");

        return htmlStr;// 返回文本字符串
    }

    /**
     * 查找指定字符中某字符串中第N次出现位置的索引
     * 
     * @param text 被查找的字符串
     * @param searchChar 要搜索的字符
     * @param order 第几次出现，从1开始
     * @return 字符第N次出现位置索引
     */
    public static int indexOfAt(CharSequence text, char searchChar, int order) {
        if (order < 1) {
            throw new IllegalArgumentException("参数 'order'必须大于或等于1");
        }
        int len = text.length();
        int curOrder = 0;
        for (int i = 0; i < len; i++) {
            if (text.charAt(i) == searchChar) {
                curOrder++;
                if (curOrder == order) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 返回一个字符串中指定分隔串之后的字符串，忽略分隔串的大小写.
     * <p>
     * 补充commons-lang 的StringUtils.substringAfter方法
     * 
     * @param str 指定字符串
     * @param separator 分隔字符串
     * @return
     */
    public static String substringAfterIgnoreCase(String str, String separator) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (separator == null) {
            return StringUtils.EMPTY;
        }
        int pos = StringUtils.indexOfIgnoreCase(str, separator);
        if (pos == StringUtils.INDEX_NOT_FOUND) {
            return StringUtils.EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * 返回一个字符串中指定分隔串之前的字符串，忽略分隔串的大小写。.
     * <p>
     * 补充commons-lang 的StringUtils.substringBefore方法
     * 
     * @param str 指定字符串
     * @param separator 分隔字符串
     * @return
     */
    public static String substringBeforeIgnoreCase(String str, String separator) {
        if (StringUtils.isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.length() == 0) {
            return StringUtils.EMPTY;
        }
        int pos = StringUtils.indexOfIgnoreCase(str, separator);
        if (pos == StringUtils.INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * 将字符串插入到另一字符串的指定位置处.如果索引大于字符长度将直接加到最后.如果索引<1加在最前
     * 
     * @param str 被插入字符串
     * @param insertStr 要插入的字符串
     * @param index 插入的索引
     * @return
     */
    public static String insertAt(String str, String insertStr, int index) {
        if (isEmpty(str)) {
            return insertStr;
        }
        if (isEmpty(insertStr)) {
            return str;
        }
        if (index > str.length()) {
            return str + insertStr;
        }
        if (index <= 0) {
            return insertStr + str;
        }
        return str.substring(0, index) + insertStr + str.substring(index);
    }

    /**
     * 从字符串中删除字符，指定起始位和长度
     * 
     * @param str 待删除字符串
     * @param startIndex 指定索引下标，包含在删除内
     * @param length 指定要删除的长度
     * @return
     */
    public static String remove(final String str, int startIndex, int length) {
        if (isNotEmpty(str)) {
            if (startIndex < 0) {
                startIndex = 0;
            }
            char[] strToCharArr = str.toCharArray();
            if (startIndex + length >= strToCharArr.length) {
                length = strToCharArr.length - startIndex;
            }
            int[] removedSub = new int[length];
            int i = 0;
            while (i < length) {
                removedSub[i] = startIndex + i;
                i++;
            }
            char[] newCharArr = ArrayUtils.removeAll(strToCharArr, removedSub);
            return new String(newCharArr);

        }
        return str;
    }

    /**
     * 删除最后一个字符
     * 
     * @param str String
     * @return 返回新的字符串
     */
    public static String removeLast(final String str) {
        return StringUtils.substring(str, 0, str.length() - 1);
    }

    /**
     * 返回一个没有-号的uuid字符串
     * 
     * @return
     */
    public static String uuid() {
        return StringUtils.remove(UUID.randomUUID().toString(), '-');
    }

    /**
     * 使用指定编码解码字符串
     * 
     * @param string 字符串
     * @param charsetName 编码集
     * @return
     * @throws TranscodeException 不支持的编码集
     */
    public static byte[] getBytes(String string, String charsetName) throws TranscodeException {
        if (string == null) {
            return null;
        }
        try {
            return string.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new TranscodeException(charsetName, e);
        }
    }

    /**
     * 使用utf-8解码字符串
     * 
     * @param string
     * @return
     */
    public static byte[] getUtf8Bytes(String string) {
        try {
            return getBytes(string, CharsetHelper.UTF_8);
        } catch (TranscodeException e) {
            return null;// 不会发生
        }
    }

    /**
     * 将byte[]使用utf-8编码为字符串
     * 
     * @param bytes byte[]
     * @return
     */
    public static String getUtf8String(byte[] bytes) {
        try {
            return new String(bytes, CharsetHelper.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return null;// 该异常不会出现
        }
    }
}
