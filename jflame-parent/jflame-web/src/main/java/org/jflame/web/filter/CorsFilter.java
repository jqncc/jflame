package org.jflame.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.jflame.toolkit.config.CommonConfigKeys;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.toolkit.util.UrlMatcher;
import org.jflame.web.util.WebUtils;

/**
 * 跨域访问过滤器
 * 
 * @author yucan.zhang
 */
public class CorsFilter extends OncePerRequestFilter {

    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String ORIGIN = "Origin";

    private String[] allowDomains;
    private String allowMethods;
    private String allowHeaders = "Origin,X-Requested-With,Content-Type,Accept";
    private Long maxAge;
    private String allowCredentials;
    private String exposeHeaders;
    private String[] allowedApiUrls = null;// 允许跨域访问的接口地址
    private String[] disallowedApiUrls = null;// 禁止跨域访问的接口地址

    @Override
    protected void doFilterInternal(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpServletRequest request = (HttpServletRequest) req;
        String origin = request.getHeader(ORIGIN);

        if (origin != null) {
            if ("*".equals(allowDomains[0]) || ArrayUtils.contains(allowDomains, origin)) {
                String requestUrl = WebUtils.getRequestPath(request);
                // 是否是禁止访问的地址
                if (ArrayUtils.isNotEmpty(disallowedApiUrls)) {
                    if (UrlMatcher.match(disallowedApiUrls, requestUrl)) {
                        if (log.isDebugEnabled()) {
                            log.debug("cors disallowed,url:{}", requestUrl);
                        }
                        return;
                    }
                }
                // 是否是允许访问的地址
                if (ArrayUtils.isNotEmpty(allowedApiUrls)) {
                    if (!UrlMatcher.match(allowedApiUrls, requestUrl)) {
                        if (log.isDebugEnabled()) {
                            log.debug("cors disallowed,url:{}", requestUrl);
                        }
                        return;
                    }
                }
                response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentials);
                response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
                if (!isPreFlightRequest(request)) {
                    if (exposeHeaders != null) {
                        response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders);
                    }

                    if (maxAge != null)
                        response.setHeader(ACCESS_CONTROL_MAX_AGE, maxAge.toString()); // 设置过期时间
                    response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, allowMethods);
                } else {
                    response.getWriter()
                            .flush();
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    protected void internalInit(FilterConfig filterConfig) {
        allowMethods = filterParam.getString(CommonConfigKeys.CORS_ALLOW_METHODS);
        String addHeaders = filterParam.getString(CommonConfigKeys.CORS_ADDHEADERS);
        if (StringHelper.isNotEmpty(addHeaders)) {
            allowHeaders = allowHeaders + "," + addHeaders;
        }
        allowDomains = filterParam.getStringArray(CommonConfigKeys.CORS_ALLOW_DOMAINS);
        maxAge = filterParam.getLong(CommonConfigKeys.CORS_MAXAGE);
        allowCredentials = filterParam.getBoolean(CommonConfigKeys.CORS_ALLOW_CREDENTIALS)
                .toString();
        exposeHeaders = filterParam.getString(CommonConfigKeys.CORS_ALLOW_EXPOSEHEADERS);
        allowedApiUrls = filterParam.getStringArray(CommonConfigKeys.CORS_ALLOW_API);
        disallowedApiUrls = filterParam.getStringArray(CommonConfigKeys.CORS_DISALLOW_API);
    }

    /**
     * Returns {@code true} if the request is a valid CORS one.
     */
    public static boolean isCorsRequest(HttpServletRequest request) {
        return (request.getHeader(ORIGIN) != null);
    }

    /**
     * Returns {@code true} if the request is a valid CORS pre-flight one.
     */
    public static boolean isPreFlightRequest(HttpServletRequest request) {
        return (isCorsRequest(request) && "OPTIONS".equalsIgnoreCase(request.getMethod())
                && request.getHeader(ACCESS_CONTROL_REQUEST_METHOD) != null);
    }

    @Override
    public void destroy() {

    }
}
