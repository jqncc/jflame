package org.jflame.context.dubbo;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.UrlMatcher;
import org.jflame.context.env.BaseConfig;

/**
 * rest协议跨域请求处理器.
 * 
 * @author yucan.zhang
 */
@PreMatching
public class RestCorsFilter extends CorsFilter {

    private final Logger logger = LoggerFactory.getLogger(RestCorsFilter.class);
    private Set<String> allowedApiUrls = null;
    private Set<String> disallowedApiUrls = null;

    public RestCorsFilter() {
        Set<String> tmpAllowedOrigins = BaseConfig.corsAllowedOrigins();
        allowedOrigins.addAll(tmpAllowedOrigins);
        if (logger.isDebugEnabled()) {
            logger.debug("cors filter allowedOrigins:{}", CollectionHelper.toString(allowedOrigins));
        }
        setAllowedHeaders(BaseConfig.corsAllowedHeader());
        allowedApiUrls = BaseConfig.corsAllowedUrls();
        disallowedApiUrls = BaseConfig.corsDisallowedUrls();
    }

    @Override
    protected void preflight(String origin, ContainerRequestContext requestContext) throws IOException {
        String requestUrl = requestContext.getUriInfo()
                .getPath();
        // 是否是禁止访问的地址
        if (CollectionHelper.isNotEmpty(disallowedApiUrls)) {
            if (UrlMatcher.match(disallowedApiUrls, requestUrl)) {
                Response.ResponseBuilder builder = Response.status(Status.FORBIDDEN);
                requestContext.abortWith(builder.build());
                return;
            }
        }
        // 是否是允许访问的地址
        if (CollectionHelper.isNotEmpty(allowedApiUrls)) {
            if (!UrlMatcher.match(allowedApiUrls, requestUrl)) {
                Response.ResponseBuilder builder = Response.status(Status.FORBIDDEN);
                requestContext.abortWith(builder.build());
                return;
            }
        }
        super.preflight(origin, requestContext);
    }

    /**
     * 使用地址模糊匹配
     */
    @Override
    protected void checkOrigin(ContainerRequestContext requestContext, String origin) {
        if (!allowedOrigins.contains("*") && !UrlMatcher.match(allowedOrigins, origin)) {
            requestContext.setProperty("cors.failure", true);
            throw new ForbiddenException("Origin not allowed: " + origin);
        }
    }

}
