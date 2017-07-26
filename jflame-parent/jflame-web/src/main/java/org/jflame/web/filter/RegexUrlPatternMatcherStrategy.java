package org.jflame.web.filter;

import java.util.regex.Pattern;

/**
 * 正则url匹配
 * 
 * @author yucan.zhang
 */
public final class RegexUrlPatternMatcherStrategy implements UrlPatternMatcherStrategy {

    private Pattern pattern;

    public RegexUrlPatternMatcherStrategy() {
    }

    public RegexUrlPatternMatcherStrategy(final String pattern) {
        this.setPattern(pattern);
    }

    public boolean matches(final String url) {
        return this.pattern.matcher(url).find();
    }

    @Override
    public void setPattern(String _pattern) {
        this.pattern = Pattern.compile(_pattern);
    }
}
