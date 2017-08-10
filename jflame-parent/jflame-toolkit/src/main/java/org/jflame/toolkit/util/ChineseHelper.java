package org.jflame.toolkit.util;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/**
 * 汉字工具类.
 * 
 * @author yucan.zhang
 */
public final class ChineseHelper {

    private final static int[] li_SecPosValue = { 1601,1637,1833,2078,2274,2302,2433,2594,2787,3106,3212,3472,3635,3722,
            3730,3858,4027,4086,4390,4558,4684,4925,5249,5590 };
    private final static char[] lc_FirstLetter = { 'a','b','c','d','e','f','g','h','j','k','l','m','n','o','p','q','r',
            's','t','w','x','y','z' };

   

    /**
     * 判断是否是汉字
     * 
     * @param oneChar 字符
     * @return true是汉字
     */
    public static boolean isChinese(char oneChar) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(oneChar);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    /**
     * 取得给定汉字串的首字母串,即声母串
     * 
     * @param str 汉字
     * @return 声母
     * @throws UnsupportedEncodingException 不支持的编码异常
     */
    public static String getAllFirstLetter(String str) throws UnsupportedEncodingException {
        if (str == null || str.isEmpty()) {
            return "";
        }

        char[] charArr = new char[str.length()];
        for (int i = 0; i < charArr.length; i++) {
            charArr[i] = getFirstLetter(str.substring(i, i + 1));
        }

        return new String(charArr);
    }

    /**
     * 取得给定汉字的首字母,即声母
     * 
     * @param chinese 汉字
     * @return 汉字的声母
     * @throws UnsupportedEncodingException 不支持的编码异常
     */
    private static char getFirstLetter(String chinese) throws UnsupportedEncodingException {
        if (chinese == null || chinese.isEmpty()) {
            throw new IllegalArgumentException("请传入中文字符");
        }
        char f = ' ';
        chinese = new String(chinese.getBytes(CharsetHelper.GBK), CharEncoding.ISO_8859_1);
        // 判断是不是汉字
        if (chinese.length() > 1) {
            int liSectorCode = (int) chinese.charAt(0); // 汉字区码
            int liPositionCode = (int) chinese.charAt(1); // 汉字位码
            liSectorCode = liSectorCode - 160;
            liPositionCode = liPositionCode - 160;
            int liSecPosCode = liSectorCode * 100 + liPositionCode; // 汉字区位码
            if (liSecPosCode > 1600 && liSecPosCode < 5590) {
                for (int i = 0; i < 23; i++) {
                    if (liSecPosCode >= li_SecPosValue[i] && liSecPosCode < li_SecPosValue[i + 1]) {
                        f = lc_FirstLetter[i];
                        break;
                    }
                }
            } else {
                // 非汉字字符,如图形符号或ASCII码
                chinese = new String(chinese.getBytes(CharEncoding.ISO_8859_1), CharsetHelper.GBK);
                f = chinese.charAt(0);
            }
        }

        return f;
    }

    /**
     * 随机产生汉字
     * 
     * @param count 生成的个数
     * @return
     */
    public static String randomChinese(int count) {
        String ret = StringUtils.EMPTY;
        Random random = new Random();
        byte[] b = new byte[2];
        int hightPos;
        int lowPos; // 定义高低位
        for (int i = 0; i < count; i++) {
            hightPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
            lowPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
            b[0] = (new Integer(hightPos).byteValue());
            b[1] = (new Integer(lowPos).byteValue());
            try {
                ret += new String(b, CharsetHelper.GBK);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    /*
     * public static void main(String[] args) throws UnsupportedEncodingException { //System.out.println("获取拼音首字母：" +
     * getAllFirstLetter("大中国南昌中大china")); System.out.println(randomChinese(4)); System.out.println(randomChinese(4));
     * System.out.println(randomChinese(4)); System.out.println(randomChinese(4)); }
     */

}
