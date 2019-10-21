package org.jflame.context.spring;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import org.jflame.context.web.WebUtils;

public final class SpringUtils {

    /**
     * 是否需要返回json数据.判断条件:<br>
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
                    .getAnnotation(RestController.class) != null) {
                return true;
            }
        }

        return false;
    }

}
