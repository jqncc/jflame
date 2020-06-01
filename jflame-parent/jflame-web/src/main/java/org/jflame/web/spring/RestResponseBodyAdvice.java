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

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

import org.jflame.commons.model.BaseResult;
import org.jflame.commons.model.CallResult;

/**
 * json返回统一封装为CallResult
 * 
 * @author yucan.zhang
 */
@ControllerAdvice
public class RestResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (BaseResult.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        return FastJsonHttpMessageConverter.class.isAssignableFrom(converterType)
                || AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType)
                || GsonHttpMessageConverter.class.isAssignableFrom(converterType);
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
