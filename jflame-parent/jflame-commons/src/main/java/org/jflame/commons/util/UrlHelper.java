package org.jflame.commons.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;

import org.jflame.commons.common.Chars;

/**
 * url工具类
 * 
 * @author yucan.zhang
 */
public final class UrlHelper {

    /**
     * 是否是一个不包含查询参数的url
     * 
     * @param urlTxt
     * @return
     */
    public static boolean isNoQueryURL(String urlTxt) {
        if (StringHelper.isEmpty(urlTxt)) {
            return false;
        }
        try {
            java.net.URL url = new java.net.URL(urlTxt);
            return url.getQuery() == null;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * 判断是否是一个正确的url地址
     * 
     * @param urlTxt url地址
     * @return
     */
    public static boolean isURL(String urlTxt) {
        if (StringHelper.isEmpty(urlTxt)) {
            return false;
        }
        try {
            new java.net.URL(urlTxt);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * 合并url，自动补充url分隔符/和纠正url.<b>不适合文件系统路径合并</b>
     * 
     * @param firstUrl 首个url，可以是绝对或相对路径,如果不以协议或/开头将补充/
     * @param relativeUrls 要合并的url，相对路径
     * @return
     */
    public static String mergeUrl(final String firstUrl, final String... relativeUrls) {
        if (StringHelper.isEmpty(firstUrl)) {
            throw new IllegalArgumentException("argument 'firstUrl' must not be null");
        }
        String fullUrl = "";
        // final char urlSplit = '/';
        if (relativeUrls.length == 0) {
            if (isAbsoluteUri(firstUrl) && Chars.SLASH != firstUrl.charAt(0)) {
                return firstUrl;
            } else {
                return Chars.SLASH + fullUrl;
            }
        }
        for (String url : relativeUrls) {
            if (url.charAt(0) != Chars.SLASH) {
                fullUrl += Chars.SLASH;
            }
            fullUrl += url;
        }
        fullUrl = fullUrl.replace('\\', Chars.SLASH)
                .replaceAll("/{2,}", "/");

        if (firstUrl.charAt(firstUrl.length() - 1) == Chars.SLASH) {
            fullUrl = firstUrl + fullUrl.substring(1);
        } else {
            fullUrl = firstUrl + fullUrl;
        }
        if (!isAbsoluteUri(fullUrl) && Chars.SLASH != fullUrl.charAt(0)) {
            fullUrl = Chars.SLASH + fullUrl;
        }

        return fullUrl;
    }

    /**
     * 判断是否是绝对路径的uri.
     * 
     * @param uri uri
     * @return
     */
    public static boolean isAbsoluteUri(String uri) {
        if (StringHelper.isEmpty(uri)) {
            return false;
        }
        return URI.create(uri)
                .isAbsolute();
    }

    /**
     * 获取url上的参数
     * 
     * @param url
     * @return
     */
    public static Map<String,String> getQueryParams(String url) {
        URI uri = URI.create(url);
        String query = uri.getQuery();
        if (query == null) {
            return null;
        }
        return StringHelper.buildMapFromUrlParam(query);
    }
}
