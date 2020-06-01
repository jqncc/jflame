package org.jflame.context.dubbo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jflame.commons.model.BaseResult;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;

/**
 * 统一rest接口json结果封装进CallResult
 * 
 * @author yucan.zhang
 */
@PreMatching
public class RestCallResultWrapperFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        if (responseContext.getMediaType() != null
                && MediaType.APPLICATION_JSON_TYPE.isCompatible(responseContext.getMediaType())) {
            // 结果为200且非BaseResult类型的子类,统一封装进CallResult返回
            if (responseContext.getStatus() == Response.Status.OK.getStatusCode() && responseContext.hasEntity()
                    && !BaseResult.class.isAssignableFrom(responseContext.getEntityClass())) {
                CallResult<Object> result = new CallResult<>();
                result.setData(responseContext.getEntity());
                responseContext.setEntity(result);
            } else if (responseContext.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                // 方法返回null时,需重设属性
                CallResult<?> result = new CallResult<>();
                result.setData(null);
                responseContext.setStatus(ResultEnum.SUCCESS.getStatus());
                responseContext.setEntity(result, responseContext.getEntityAnnotations(),
                        MediaType.APPLICATION_JSON_TYPE);
            }
        }
    }

}
