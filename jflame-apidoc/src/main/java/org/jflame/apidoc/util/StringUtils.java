package org.jflame.apidoc.util;

import java.net.URI;

public final class StringUtils {

    /**
     * 判断字符串不为null或长度等于0
     * 
     * @param text
     * @return
     */
    public static <T> boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    /**
     * 判断字符串不为null且长度大于0
     * 
     * @param text
     * @return
     */
    public static <T> boolean isNotEmpty(String text) {
        return text != null && !text.isEmpty();
    }

    /**
     * 合并url，自动补充url分隔符/和纠正url.<b>不适合文件系统路径合并</b>
     * 
     * @param firstUrl 首个url，可以是绝对或相对路径,如果不以协议或/开头将补充/
     * @param relativeUrls 要合并的url，相对路径
     * @return
     */
    public static String mergeUrl(final String firstUrl, final String... relativeUrls) {
        if (isEmpty(firstUrl)) {
            throw new IllegalArgumentException("argument 'firstUrl' must not be null");
        }
        String fullUrl = "";
        final char urlSplit = '/';
        if (relativeUrls.length == 0) {
            if (isAbsoluteUrl(firstUrl) && urlSplit != firstUrl.charAt(0)) {
                return firstUrl;
            } else {
                return urlSplit + fullUrl;
            }
        }
        for (String url : relativeUrls) {
            if (url.charAt(0) != urlSplit) {
                fullUrl += urlSplit;
            }
            fullUrl += url;
        }
        fullUrl = fullUrl.replace('\\', urlSplit).replaceAll("/{2,}", "/");

        if (firstUrl.charAt(firstUrl.length() - 1) == urlSplit) {
            fullUrl = firstUrl + fullUrl.substring(1);
        } else {
            fullUrl = firstUrl + fullUrl;
        }
        if (!isAbsoluteUrl(fullUrl) && urlSplit != fullUrl.charAt(0)) {
            fullUrl = urlSplit + fullUrl;
        }

        return fullUrl;
    }

    /**
     * 判断是否是绝对路径的url.
     * 
     * @param url url
     * @return
     */
    public static boolean isAbsoluteUrl(String url) {
        if (isEmpty(url)) {
            return false;
        }
        return URI.create(url).isAbsolute();
    }
}
