package org.jflame.context.auth.model;

import java.io.Serializable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.util.UrlMatcher;

/**
 * url权限表示实体类
 * 
 * @author yucan.zhang
 */
public interface UrlPermission extends Serializable {

    public String getFunCode();

    public String[] getFunUrls();

    default public boolean isExistMatchedUrl(String checkUrl) {
        boolean isMatched = false;
        String[] urls = getFunUrls();
        if (ArrayUtils.isNotEmpty(urls)) {
            for (String funUrl : urls) {
                isMatched = matchUrl(funUrl, checkUrl);
                if (isMatched) {
                    isMatched = true;
                    break;
                }
            }
        }
        return isMatched;
    }

    public static boolean matchUrl(String pattern, String url) {
        boolean isMatched = false;
        final String urlSpit = "/";
        /*if (pattern.endsWith(urlSpit)) {
            pattern = StringHelper.removeLast(pattern);
        }
        if (url.endsWith(urlSpit)) {
            url = StringHelper.removeLast(url);
        }*/
        pattern = StringUtils.removeEnd(pattern, urlSpit);
        url = StringUtils.removeEnd(url, urlSpit);
        if (pattern.equals(url)) {
            isMatched = true;
        } else {
            isMatched = UrlMatcher.match(pattern, url);
        }
        return isMatched;
    }
}
