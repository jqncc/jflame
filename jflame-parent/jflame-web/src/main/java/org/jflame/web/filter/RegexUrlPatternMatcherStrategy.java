package org.jflame.web.filter;

import java.util.regex.Pattern;

/**
 * 正则url匹配
 * 
 * @author yucan.zhang
 */
public final class RegexUrlPatternMatcherStrategy implements UrlPatternMatcherStrategy {

    private Pattern[] patterns;

    public RegexUrlPatternMatcherStrategy() {
    }

    public RegexUrlPatternMatcherStrategy(final String... patterns) {
        this.setPattern(patterns);
    }

    public boolean matches(final String url) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(url)
                    .find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setPattern(String... _patterns) {
        patterns = new Pattern[_patterns.length];
        for (int i = 0; i < _patterns.length; i++) {
            patterns[i] = Pattern.compile(_patterns[i]);
        }
    }
}
