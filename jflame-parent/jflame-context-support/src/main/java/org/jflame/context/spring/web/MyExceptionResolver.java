package org.jflame.context.spring.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import org.jflame.commons.common.bean.BaseResult;
import org.jflame.commons.common.bean.CallResult;
import org.jflame.commons.common.bean.CallResult.ResultEnum;
import org.jflame.context.spring.SpringUtils;
import org.jflame.context.web.WebUtils;

/**
 * 统一异常处理类. <br>
 * 
 * @see MyExceptionHandler
 * @author zyc
 */
@SuppressWarnings("rawtypes")
@Order(-100)
public class MyExceptionResolver extends SimpleMappingExceptionResolver {

    private final ModelAndView defaultErrorJsonView = ErrorJsonView.view(new CallResult(ResultEnum.SERVER_ERROR));
    private final ModelAndView defaultParamErrorJsonView = ErrorJsonView.view(new CallResult(ResultEnum.PARAM_ERROR));

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        String viewName = determineViewName(ex, request);
        if (viewName != null) {
            Integer statusCode = determineStatusCode(request, viewName);
            if (statusCode != null) {
                applyStatusCodeIfPossible(request, response, statusCode);
            }
        }

        if (SpringUtils.isJsonResult(request, handler)) {
            // 请求方法未使用BindingResult保存验证结果时,将抛出BindException异常,由此处统一处理
            if (ex instanceof BindException) {
                // 参数异常只记录信息,不记录大量异常堆栈
                logDebugMsg(request, ex.getMessage());
                BindException validEx = (BindException) ex;
                CallResult errResult = new CallResult();
                BaseController.convertError(validEx.getBindingResult(), errResult);
                return ErrorJsonView.view(errResult);
            } else if (ex instanceof BaseResult) {
                logDebugMsg(request, ex.getMessage());
                BaseResult errResult = (BaseResult) ex;
                return ErrorJsonView.view(new CallResult(errResult.getStatus(), errResult.getMessage()));
            } else if (ex instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) ex;
                List<String> msgs = cve.getConstraintViolations()
                        .stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toList());
                String errMsg = String.join(";", msgs);
                logDebugMsg(request, errMsg);
                return ErrorJsonView.view(CallResult.paramError(errMsg));
            } else if (ex instanceof ValidationException) {
                logDebugMsg(request, ex.getMessage());
                return ErrorJsonView.view(CallResult.paramError(ex.getMessage()));
            } else if (isHandleBadRequest(ex)) {
                logDebugMsg(request, ex.getMessage());
                if (ex instanceof MethodArgumentNotValidException) {
                    MethodArgumentNotValidException validEx = (MethodArgumentNotValidException) ex;
                    CallResult errResult = new CallResult();
                    BaseController.convertError(validEx.getBindingResult(), errResult);
                    return ErrorJsonView.view(errResult);
                } else {
                    return defaultParamErrorJsonView;
                }
            } else {
                logger.error("", ex);
                return defaultErrorJsonView;
            }
        } else {
            try {
                if (ex instanceof HttpRequestMethodNotSupportedException) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getMessage());
                    return new ModelAndView();
                } else if (isHandleBadRequest(ex)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                    return new ModelAndView();
                } else if (ex instanceof NoHandlerFoundException) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return new ModelAndView();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.error("", ex);
        }
        return viewName != null ? getModelAndView(viewName, ex, request) : null;
    }

    private static class ErrorJsonView implements View {

        private CallResult<?> error;

        public ErrorJsonView(CallResult<?> error) {
            this.error = error;
        }

        @Override
        public String getContentType() {
            return MediaType.APPLICATION_JSON_VALUE;
        }

        @Override
        public void render(Map<String,?> model, HttpServletRequest request, HttpServletResponse response)
                throws Exception {
            WebUtils.outJson(response, this.error);
        }

        public static ModelAndView view(CallResult<?> error) {
            return new ModelAndView(new ErrorJsonView(error));
        }
    }

    void logDebugMsg(HttpServletRequest request, String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("error,url:%s,message:%s", request.getServletPath(), msg));
        }
    }

    ModelAndView handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        String[] supportedMethods = ex.getSupportedMethods();
        if (supportedMethods != null) {
            response.setHeader("Allow", StringUtils.arrayToDelimitedString(supportedMethods, ", "));
        }
        try {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getMessage());
            return new ModelAndView();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否是参数缺失或参数类型转换异常的请求
     * 
     * @param ex
     * @return
     */
    private boolean isHandleBadRequest(Exception ex) {
        return ex instanceof MethodArgumentNotValidException || ex instanceof TypeMismatchException
                || ex instanceof IllegalArgumentException || ex instanceof MissingServletRequestParameterException
                || ex instanceof MissingPathVariableException;
    }

    /**
     * 设置异常处理优先级高于DefaultHandlerExceptionResolver
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
