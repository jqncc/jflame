package org.jflame.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.config.DefaultConfigKeys;
import org.jflame.web.util.WebUtils;

/**
 * 按匹配规则忽略过滤url的Filter抽象基类,使用正则表达式匹配,参数:<br>
 * 1.ignoreStatic[可选] 是否忽略静态资源文件,默认为true;<br>
 * 2.ignoreUrlPattern[可选] 要忽略的URL正则;<br>
 * 
 * @author yucan.zhang
 */
public abstract class IgnoreUrlMatchFilter extends OncePerRequestFilter {

    private boolean ignoreStatic;
    private String ignoreUrlPattern;
    protected UrlPatternMatcherStrategy matcher;

    @Override
    protected final void doFilterInternal(ServletRequest req, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        String requestUrl = WebUtils.getRequestPath(request);
        if (requestUrl != null && isIgnoreUrl(requestUrl)) {
            chain.doFilter(request, response);
        } else {
            doInternalFilter(req, response, chain);
        }
    }

    @Override
    protected final void internalInit(FilterConfig filterConfig) {
        ignoreStatic = filterParam.getBoolean(DefaultConfigKeys.IGNORE_STATIC);
        ignoreUrlPattern = filterParam.getString(DefaultConfigKeys.IGNORE_PATTERN);
        if (StringHelper.isNotEmpty(ignoreUrlPattern)) {
            matcher = new RegexUrlPatternMatcherStrategy(ignoreUrlPattern);
        }
    }

    protected boolean isIgnoreUrl(String url) {
        if (ignoreStatic && isWebStatic(url)) {
            return true;
        }
        if (matcher != null) {
            return matcher.matches(url);
        }
        return false;
    }

    /**
     * 判断是否是web静态文件
     * 
     * @param requestUrl 请求路径
     * @return
     */
    private boolean isWebStatic(String requestUrl) {
        if (requestUrl == null) {
            return false;
        }
        String ext = FileHelper.getExtension(requestUrl, false);
        return ArrayUtils.contains(WebUtils.WEB_STATIC_EXTS, ext);
    }

    public boolean isIgnoreStatic() {
        return ignoreStatic;
    }

    public String getIgnoreUrlPattern() {
        return ignoreUrlPattern;
    }

    protected abstract void doInternalFilter(ServletRequest req, ServletResponse response, FilterChain chain)
            throws ServletException, IOException;

    protected abstract void doInternalInit(FilterConfig filterConfig);
}
