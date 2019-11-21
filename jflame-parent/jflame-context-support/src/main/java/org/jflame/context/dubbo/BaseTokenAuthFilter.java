package org.jflame.context.dubbo;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jflame.commons.common.bean.CallResult;
import org.jflame.commons.common.bean.CallResult.ResultEnum;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.UrlMatcher;
import org.jflame.commons.util.file.FileHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rest接口身份验证抽象父类
 * 
 * @author yucan.zhang
 */
public abstract class BaseTokenAuthFilter implements ContainerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(BaseTokenAuthFilter.class);
    private CallResult<?> result = new CallResult<>(ResultEnum.NO_AUTH);
    private final String DEFAULT_TOKEN_HEADER = "Authorization";

    protected String getToken(ContainerRequestContext requestContext) {
        return requestContext.getHeaderString(DEFAULT_TOKEN_HEADER);
    }

    protected Set<String> getIgnoreUrls() {
        return null;
    }

    protected abstract boolean doAuthenticate(String requestUrl, String token, ContainerRequestContext requestContext);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestUrl = requestContext.getUriInfo()
                .getPath();
        String authToken = getToken(requestContext);
        if (requestUrl.charAt(0) == FileHelper.UNIX_SEPARATOR) {
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
