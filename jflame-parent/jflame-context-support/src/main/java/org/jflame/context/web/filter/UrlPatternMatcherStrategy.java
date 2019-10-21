package org.jflame.context.web.filter;

/**
 * url匹配策略接口
 * 
 * @author yucan.zhang
 */
public interface UrlPatternMatcherStrategy {

    /**
     * 判断url是否匹配
     * 
     * @param url
     * @return
     */
    boolean matches(String url);

    /**
     * 匹配模板
     * 
     * @param pattern
     */
    void setPattern(String... pattern);
}
