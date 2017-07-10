package org.jflame.mvc.support;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.common.bean.CallResult;
import org.jflame.toolkit.common.bean.CallResult.ResultEnum;
import org.jflame.toolkit.util.JsonHelper;
import org.jflame.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

/**
 * 统一异常处理类
 * 
 * @author zyc
 */
public class MyExceptionResolver extends SimpleMappingExceptionResolver {

    private final String err_json;
    private final Logger log = LoggerFactory.getLogger(MyExceptionResolver.class);

    public MyExceptionResolver() {
        CallResult errResult = new CallResult(ResultEnum.SERVER_ERROR);
        err_json = JsonHelper.toJson(errResult);
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        String viewName = determineViewName(ex, request);
        log.error(StringUtils.EMPTY, ex);// 日志记录异常
        if (viewName != null) {
            Integer statusCode = determineStatusCode(request, viewName);
            if (statusCode != null) {
                applyStatusCodeIfPossible(request, response, statusCode);
            }
            if (jsonResult(request, handler)) {
                // json请求返回json格式错误
                try {
                    WebUtils.outJson(response, err_json);
                } catch (IOException e) {
                    log.error("", e);
                }
                return null;

            } else {
                return getModelAndView(viewName, ex, request);
            }
        }
        return null;
    }

    boolean jsonResult(HttpServletRequest request, Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (handlerMethod.getMethodAnnotation(ResponseBody.class) != null
                || handlerMethod.getBeanType().getAnnotation(RestController.class) != null) {
            return true;
        }
        if (WebUtils.isAjaxRequest(request)) {
            return true;
        } else {
        }
        return false;
    }
}
