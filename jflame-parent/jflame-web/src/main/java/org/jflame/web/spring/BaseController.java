package org.jflame.web.spring;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import org.jflame.commons.model.CallResult;
import org.jflame.context.auth.context.UserContext;
import org.jflame.context.auth.context.UserContextHolder;
import org.jflame.context.auth.model.LoginUser;
import org.jflame.context.spring.converter.MyDateFormatter;
import org.jflame.context.spring.converter.MyTemporalFormatter;
import org.jflame.web.WebUtils;

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
    public static void convertError(Errors error, CallResult<?> result) {
        SpringWebUtils.convertError(error, result);
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

    /**
     * 从UserContext获取当前登录用户,没有用户信息时返回null
     * 
     * @return
     */
    protected LoginUser getLoginUser() {
        return UserContextHolder.getContext()
                .getUser();
    }

    /**
     * 保存当前登录用户信息到session并绑定到用户上下文UserContext
     * 
     * @param curUser 登录用户
     * @param session HttpSession
     */
    protected void saveLoginUser(LoginUser curUser, HttpSession session) {
        UserContext ctx = UserContextHolder.getContext();
        ctx.setUser(curUser);
        UserContextHolder.setContext(ctx);
        session.setAttribute(UserContext.CONTEXT_KEY, ctx);
    }

    /**
     * 生成新的会话,并保存登录用户信息到会话
     * 
     * @param curUser 登录用户
     * @param request HttpServletRequest
     */
    protected void newSessionAndSaveLoginUser(LoginUser curUser, HttpServletRequest request) {
        HttpSession session = WebUtils.logoutAndNewSession(request);
        saveLoginUser(curUser, session);
    }

}
