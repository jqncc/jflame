package org.jflame.web.spring;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import org.jflame.commons.model.BaseResult;
import org.jflame.commons.model.CallResult;

/**
 * json返回统一封装为CallResult
 * 
 * @author yucan.zhang
 */
@ControllerAdvice
public class RestResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final String jsonConverterName = "FastJsonHttpMessageConverter";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (BaseResult.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        if (AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType)
                || GsonHttpMessageConverter.class.isAssignableFrom(converterType)) {
            return true;
        }
        // fastjson非spring内置converter不一定使用,所以使用类名判断.
        if (jsonConverterName.equals(converterType.getSimpleName())
                || jsonConverterName.equals(converterType.getSuperclass()
                        .getSimpleName())) {
            return true;
        }

        return false;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!(body instanceof BaseResult)) {
            return CallResult.ok(body);
        }
        return body;
    }

}
