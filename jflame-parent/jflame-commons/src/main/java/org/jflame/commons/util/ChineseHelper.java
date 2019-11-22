package org.jflame.commons.util;

import java.util.Random;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import org.jflame.commons.exception.ConvertException;

/**
 * 汉字工具类.
 * 
 * @author yucan.zhang
 */
public final class ChineseHelper {

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
     * 取得给定汉字串拼音首字母,即声母串.
     * 
     * @param chineseStr 汉字
     * @return 声母
     */
    public static String getLetterForshort(String chineseStr) {
        try {
            return PinyinHelper.getShortPinyin(chineseStr);
        } catch (PinyinException e) {
            throw new ConvertException(e);
        }
    }

    /**
     * 汉字转拼音(带声调)
     * 
     * @param chineseStr
     * @return
     */
    public static String convertToPinyin(String chineseStr) {
        try {
            return PinyinHelper.convertToPinyinString(chineseStr, " ");
        } catch (PinyinException e) {
            throw new ConvertException(e);
        }
    }

    /**
     * 汉字转拼音(不带声调)
     * 
     * @param chineseStr
     * @return
     */
    public static String convertToPinyinNoTone(String chineseStr) {
        try {
            return PinyinHelper.convertToPinyinString(chineseStr, " ", PinyinFormat.WITHOUT_TONE);
        } catch (PinyinException e) {
            throw new ConvertException(e);
        }
    }

    /**
     * 简体字转为繁体字
     * 
     * @param str 简体字
     * @return
     */
    public static String simplifiedChineseToBig5(String str) {
        return com.github.stuxuhai.jpinyin.ChineseHelper.convertToTraditionalChinese(str);
    }

    /**
     * 繁体字转为简体字
     * 
     * @param str 繁体字
     * @return
     */
    public static String big5ToSimplifiedChinese(String str) {
        return com.github.stuxuhai.jpinyin.ChineseHelper.convertToSimplifiedChinese(str);
    }

    /**
     * 随机产生汉字
     * 
     * @param count 生成的个数
     * @return
     */
    public static String randomChinese(int count) {
        String ret = "";
        Random random = new Random();
        byte[] b = new byte[2];
        int hightPos;
        int lowPos; // 定义高低位
        for (int i = 0; i < count; i++) {
            hightPos = (176 + Math.abs(random.nextInt(39))); // 获取高位值
            lowPos = (161 + Math.abs(random.nextInt(93))); // 获取低位值
            b[0] = (new Integer(hightPos).byteValue());
            b[1] = (new Integer(lowPos).byteValue());
            ret += new String(b, CharsetHelper.GBK_18030);
        }
        return ret;
    }

}
