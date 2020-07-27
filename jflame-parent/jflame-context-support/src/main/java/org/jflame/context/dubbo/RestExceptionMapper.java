package org.jflame.context.dubbo;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.protocol.rest.support.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.model.BaseResult;
import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;

/**
 * 拦截rest服务所有异常转为统一json格式返回
 * 
 * @author yucan.zhang
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<Exception> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Context
    protected HttpServletRequest request;

    @Override
    public Response toResponse(Exception exception) {
        CallResult<?> result = new CallResult<>(ResultEnum.SERVER_ERROR);
        ResponseBuilder responseBuilder = Response.status(Status.SERVICE_UNAVAILABLE);
        String apiUrl = request.getPathInfo();
        /**
         * 特殊处理BusinessException,IllegalArgumentException和参数验证异常
         */
        if (exception instanceof BaseResult) {
            BaseResult ex = (BaseResult) exception;
            result.setMessage(ex.getMessage());
            if (ex.getStatus() > 0) {
                result.setStatus(ex.getStatus());
                responseBuilder = Response.ok();
            }
            logger.error("请求:{},ex:{}", apiUrl, exception.getMessage());
        } else if (exception instanceof javax.ws.rs.NotFoundException) {
            result.setResult(Status.NOT_FOUND.getStatusCode(), "资源不存在");
            responseBuilder = Response.status(Status.NOT_FOUND);
        } else if (exception instanceof javax.ws.rs.NotAllowedException) {
            result.setResult(Status.METHOD_NOT_ALLOWED.getStatusCode(), "method not allowed");
            responseBuilder = Response.status(Status.METHOD_NOT_ALLOWED);
        } else if (exception instanceof IllegalArgumentException) {
            result.setResult(Status.BAD_REQUEST.getStatusCode(), exception.getMessage());
            responseBuilder = Response.ok();
            logger.error("请求:{},ex:{}", exception.getMessage());
        } else if (exception instanceof ConstraintViolationException) {
            responseBuilder = Response.ok();
            handleValidateException((ConstraintViolationException) exception, result);
            if (logger.isDebugEnabled()) {
                logger.debug("请求:{},ex:{}", apiUrl, result.getMessage());
            }
        } else if (exception.getCause() instanceof ConstraintViolationException) {
            responseBuilder = Response.ok();
            handleValidateException((ConstraintViolationException) exception.getCause(), result);
            if (logger.isDebugEnabled()) {
                logger.debug("请求:{},ex:{}", apiUrl, result.getMessage());
            }
        } else if (exception instanceof ValidationException) {
            responseBuilder = Response.ok();
            result.setResult(Status.BAD_REQUEST.getStatusCode(), exception.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("请求:{},ex:{}", apiUrl, result.getMessage());
            }
        } else {
            logger.error("请求:" + apiUrl, exception);
        }
        return responseBuilder.entity(result)
                .type(ContentType.APPLICATION_JSON_UTF_8)
                .build();
    }

    /**
     * 处理参数检验异常
     */
    @SuppressWarnings({ "rawtypes" })
    void handleValidateException(ConstraintViolationException cve, CallResult result) {
        result.setStatus(ResultEnum.PARAM_ERROR.getStatus());
        Map<String,String> errMap = new HashMap<>();
        for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
            errMap.put(cv.getPropertyPath()
                    .toString(), cv.getMessage());
        }
        result.setMessage(StringUtils.join(errMap.values(), ';'));
        // result.setData(errMap);
        /* return Response.status(Response.Status.OK)
                .entity(result)
                .type(ContentType.APPLICATION_JSON_UTF_8)
                .build();*/
    }

}
