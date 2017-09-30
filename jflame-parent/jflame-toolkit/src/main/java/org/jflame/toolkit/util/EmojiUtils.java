package org.jflame.toolkit.util;

public final class EmojiUtils {

    // public static final String EMOJI_REGEX = "[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]";

    /**
     * 检测是否有emoji字符
     * 
     * @param source
     * @return 一旦含有就抛出
     */
    public static boolean hasEmoji(String source) {
        if (StringHelper.isEmpty(source)) {
            return false;
        }
        char[] sourceChars = source.toCharArray();
        for (char c : sourceChars) {
            if (isNotEmojiChar(c)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNotEmojiChar(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
                || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));

    }

    /**
     * 过滤emoji或者 其他非文字类型的字符
     * 
     * @param source
     * @return
     */
    public static String filterEmoji(String source) {
        if (StringHelper.isEmpty(source)) {
            return source;
        }

        char[] sourceChars = source.toCharArray();
        StringBuilder buf = new StringBuilder(sourceChars.length);
        for (char c : sourceChars) {
            if (isNotEmojiChar(c)) {
                buf.append(c);
            }
        }
        return buf.toString();
    }

}
