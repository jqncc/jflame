package org.jflame.context.web.filter;

import org.jflame.toolkit.util.UrlMatcher;

/**
 * ant风格的url匹配,同spring的url匹配
 * 
 * @author yucan.zhang
 */
public final class AntStyleUrlPatternMatcherStrategy implements UrlPatternMatcherStrategy {

    private String[] patterns;

    public AntStyleUrlPatternMatcherStrategy() {
    }

    public AntStyleUrlPatternMatcherStrategy(final String[] patterns) {
        this.setPattern(patterns);
    }

    public boolean matches(final String url) {
        return UrlMatcher.match(patterns, url);
    }

    @Override
    public void setPattern(String... _patterns) {
        patterns = _patterns;
    }

}
