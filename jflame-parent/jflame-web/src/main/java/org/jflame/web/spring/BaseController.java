package org.jflame.web.spring;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import org.jflame.commons.common.bean.CallResult;
import org.jflame.commons.common.bean.CallResult.ResultEnum;
import org.jflame.context.auth.model.LoginUser;
import org.jflame.context.spring.converter.MyDateFormatter;
import org.jflame.context.spring.converter.MyTemporalFormatter;

/**
 * controller基类
 * 
 * @author yucan.zhang
 */
public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @InitBinder
    protected void ininBinder(WebDataBinder binder) {
        // 注册时间转换器
        binder.addCustomFormatter(new MyDateFormatter<>(java.util.Date.class), java.util.Date.class);
        binder.addCustomFormatter(new MyDateFormatter<>(java.sql.Timestamp.class), java.sql.Timestamp.class);
        binder.addCustomFormatter(new MyDateFormatter<>(java.sql.Date.class), java.sql.Date.class);
        binder.addCustomFormatter(new MyDateFormatter<>(java.sql.Time.class), java.sql.Time.class);

        binder.addCustomFormatter(new MyTemporalFormatter<>(LocalDateTime.class), LocalDateTime.class);
        binder.addCustomFormatter(new MyTemporalFormatter<>(LocalDate.class), LocalDate.class);
        binder.addCustomFormatter(new MyTemporalFormatter<>(LocalTime.class), LocalTime.class);
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

    /**
     * 获取HttpSession
     * 
     * @param request
     * @return
     */
    protected HttpSession getSession(HttpServletRequest request) {
        return request.getSession(false);
    }

    protected LoginUser getLoginUser() {
        return WebContextHolder.getLoginUser();
    }

}
