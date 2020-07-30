package org.jflame.web.spring;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.web.WebUtils;

public final class SpringWebUtils {

    /**
     * 接口是否需要返回json数据.判断条件:<br>
     * 1.如果是accept是json,或带有x-requested-with的请求头 <br>
     * 2.contoller方法有ResponseBody注解和类上有RestController注解
     * 
     * @param request HttpServletRequest
     * @param handler handler
     * @return
     */
    public static boolean isJsonResult(HttpServletRequest request, Object handler) {
        if (WebUtils.isJsonRequest(request) || WebUtils.isAjaxRequest(request)) {
            return true;
        }
        if (handler != null && handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.getMethodAnnotation(ResponseBody.class) != null || handlerMethod.getBeanType()
                    .isAnnotationPresent(RestController.class)
                    || handlerMethod.getBeanType()
                            .isAnnotationPresent(ResponseBody.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 将errors对象转换为CallResult输出,验证错误转为map
     * 
     * @param error 错误对象
     * @param result
     * @see org.springframework.validation.Errors
     */
    @SuppressWarnings({ "rawtypes","unchecked" })
    public static void convertError(Errors error, CallResult result) {
        if (error.hasErrors()) {
            result.setStatus(ResultEnum.PARAM_ERROR.getStatus());
            Map<String,String> errMap = new HashMap<>();
            String errorMsg = "";
            if (error.hasGlobalErrors()) {
                String globalMsg = "";
                for (ObjectError ferr : error.getGlobalErrors()) {
                    globalMsg = globalMsg + ferr.getDefaultMessage() + ";";
                }
                errorMsg += globalMsg;
                errMap.put("_global_msg", globalMsg);
            }
            if (error.hasFieldErrors()) {
                for (FieldError ferr : error.getFieldErrors()) {
                    errMap.put(ferr.getField(), ferr.getDefaultMessage());
                    errorMsg = errorMsg + ferr.getDefaultMessage() + ";";
                }
            }
            result.setMessage(errorMsg);
            result.setData(errMap);
        }
    }
}
