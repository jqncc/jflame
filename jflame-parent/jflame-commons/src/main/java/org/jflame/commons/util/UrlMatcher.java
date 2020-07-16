package org.jflame.commons.util;

import java.util.Collection;

/**
 * ant风格url匹配工具类,同spring
 * <p>
 * ant匹配规则:<br>
 * ? 匹配一个字符<br>
 * * 匹配0个及以上字符<br>
 * ** 匹配0个及以上目录directories
 * </p>
 * <p>
 * 示例:<br>
 * com/t?st.jsp-匹配: com/test.jsp,com/tast.jsp,com/txst.jsp<br>
 * com/*.jsp-匹配: com文件夹下的全部.jsp文件<br>
 * com/<code>**</code>/test.jsp-匹配: com文件夹和子文件夹下的全部.jsp文件,<br>
 * org/springframework/<code>**</code>/*.jsp - 匹配: org/springframework文件夹和子文件夹下的全部.jsp文件<br>
 * org/<code>**</code>/servlet/bla.jsp- 匹配: org/springframework/servlet/bla.jsp
 * </p>
 * 
 * @author yucan.zhang
 */
public final class UrlMatcher {

    private static AntPathMatcher matcher = new AntPathMatcher();

    public static boolean match(String pattern, String url) {
        return matcher.match(pattern, url);
    }

    public static boolean match(Collection<String> patterns, String url) {
        for (String pattern : patterns) {
            if (matcher.match(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    public static boolean match(String[] patterns, String url) {
        for (String pattern : patterns) {
            if (matcher.match(pattern, url)) {
                return true;
            }
        }
        return false;
    }
}
