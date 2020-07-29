package org.jflame.context.dubbo;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.config.PropertiesConfigHolder;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.commons.model.Chars;
import org.jflame.commons.util.ArrayHelper;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.UrlMatcher;

/**
 * rest接口身份验证抽象父类
 * 
 * @author yucan.zhang
 */
public abstract class BaseTokenAuthFilter implements ContainerRequestFilter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final CallResult<?> result = new CallResult<>(ResultEnum.NO_AUTH);
    private final String DEFAULT_TOKEN_HEADER = "Authorization";
    private Set<String> ignoreUrlSet = null;

    public BaseTokenAuthFilter() {
        String[] ignoreUrls = PropertiesConfigHolder.getStringArray("rest.auth.exclude");
        if (ArrayHelper.isNotEmpty(ignoreUrls)) {
            ignoreUrlSet = new HashSet<>(ignoreUrls.length);
            Collections.addAll(ignoreUrlSet, ignoreUrls);
        }
    }

    protected String getToken(ContainerRequestContext requestContext) {
        return requestContext.getHeaderString(DEFAULT_TOKEN_HEADER);
    }

    public Set<String> getIgnoreUrls() {
        return ignoreUrlSet;
    }

    protected abstract boolean doAuthenticate(String requestUrl, String token, ContainerRequestContext requestContext);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestUrl = requestContext.getUriInfo()
                .getPath();
        String authToken = getToken(requestContext);
        if (requestUrl.charAt(0) == Chars.SLASH) {
            requestUrl = requestUrl.substring(1);
        }
        logger.debug("auth filter:{}", requestUrl);
        if (!ignoreMatchUrl(requestUrl)) {
            boolean isOk = false;
            if (StringHelper.isNotEmpty(authToken)) {
                isOk = doAuthenticate(requestUrl, authToken, requestContext);
            }
            if (!isOk) {
                logger.warn("client auth failed,url:{},token:{}", requestUrl, authToken);
                Response noAuthResp = Response.status(Response.Status.OK)
                        .entity(result)
                        .type(MediaType.APPLICATION_JSON)
                        .build();
                requestContext.abortWith(noAuthResp);
                return;
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("ignore auth url:{}", requestUrl);
            }
        }
    }

    private boolean ignoreMatchUrl(String requestUrl) {
        Set<String> ignoreUrls = getIgnoreUrls();
        if (CollectionHelper.isNotEmpty(ignoreUrls)) {
            return UrlMatcher.match(ignoreUrls, requestUrl);
        }
        return false;
    }

}
