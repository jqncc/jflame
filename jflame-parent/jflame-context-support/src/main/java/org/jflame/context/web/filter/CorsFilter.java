package org.jflame.context.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.config.ConfigKey;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.UrlMatcher;
import org.jflame.context.env.BaseConfig;
import org.jflame.context.web.WebUtils;

/**
 * 跨域访问处理过滤器
 * 
 * @author yucan.zhang
 */
public class CorsFilter extends OncePerRequestFilter {

    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    // private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    // private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    // private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    private static final String ORIGIN = "Origin";

    private String[] allowDomains;
    private String allowMethods;
    private String allowHeaders = "Origin,X-Requested-With,Content-Type,Accept";
    // private Long maxAge;
    // private String allowCredentials;
    // private String exposeHeaders;
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
                if (isDisallowUrl(requestUrl)) {
                    return;
                }

                response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
                if (!isPreFlightRequest(request)) {
                    /*if (exposeHeaders != null) {
                        response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders);
                    }
                    if (maxAge != null)
                     response.setHeader(ACCESS_CONTROL_MAX_AGE, maxAge.toString()); // 设置过期时间
                     **/
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

    /**
     * 是否禁止跨域访问的地址
     * 
     * @param requestUrl
     * @return
     */
    private boolean isDisallowUrl(String requestUrl) {
        // 先判断是否属于禁止的
        if (ArrayUtils.isNotEmpty(disallowedApiUrls)) {
            if (UrlMatcher.match(disallowedApiUrls, requestUrl)) {
                if (log.isDebugEnabled()) {
                    log.debug("cors disallowed,url:{}", requestUrl);
                }
                return true;
            }
        }
        // 再判断是否属于允许范围的
        if (ArrayUtils.isNotEmpty(allowedApiUrls)) {
            if (!UrlMatcher.match(allowedApiUrls, requestUrl)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void internalInit(FilterConfig filterConfig) {
        allowMethods = getStringParam(BaseConfig.CFG_CORS_ALLOW_METHODS);
        String addHeaders = getStringParam(BaseConfig.CFG_CORS_ADDHEADERS);
        if (StringHelper.isNotEmpty(addHeaders)) {
            allowHeaders = allowHeaders + "," + addHeaders;
        }
        allowDomains = filterParam.getStringArray(BaseConfig.CFG_CORS_ALLOW_DOMAINS);
        /*maxAge = filterParam.getLong(BaseConfig.CFG_CORS_MAXAGE);
        allowCredentials = filterParam.getBoolean(BaseConfig.CFG_CORS_ALLOW_CREDENTIALS)
                .toString();
        exposeHeaders = filterParam.getString(BaseConfig.CFG_CORS_ALLOW_EXPOSEHEADERS);*/
        allowedApiUrls = getStringArrayParam(BaseConfig.CFG_CORS_ALLOW_API);
        disallowedApiUrls = getStringArrayParam(BaseConfig.CFG_CORS_DISALLOW_API);
    }

    /**
     * 获取配置参数,先从filter配置找,再从配置文件找
     * 
     * @param cfgKey 配置参数名
     * @return 返回字符串
     */
    private String getStringParam(ConfigKey<String> cfgKey) {
        String cfgValue = filterParam.getString(cfgKey);
        if (StringHelper.isEmpty(cfgValue)) {
            cfgValue = BaseConfig.getString(cfgKey);
        }
        return cfgValue;
    }

    private String[] getStringArrayParam(ConfigKey<String[]> cfgKey) {
        String[] cfgValue = filterParam.getStringArray(cfgKey);
        if (ArrayUtils.isEmpty(cfgValue)) {
            cfgValue = BaseConfig.getStringArray(cfgKey);
        }
        return cfgValue;
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
