package org.jflame.web.spring;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.jflame.context.auth.model.LoginUser;
import org.jflame.context.auth.model.UrlPermission;
import org.jflame.web.WebUtils;

/**
 * 基于spring mvc的web context holder
 * 
 * @author yucan.zhang
 */
public final class WebContextHolder {

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra != null) {
            return sra.getRequest();
        }
        return null;
    }

    /**
     * SpringMvc下获取session
     * 
     * @return
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            HttpSession session = request.getSession();
            return session;
        }
        return null;
    }

    /**
     * 从WebContextHolder获取session中的用户信息LoginUser
     * 
     * @return 当前登录用户
     */
    public static LoginUser getLoginUser() {
        HttpSession session = WebContextHolder.getSession();
        if (session != null) {
            return (LoginUser) session.getAttribute(WebUtils.SESSION_USER_KEY);
        }
        return null;
    }

    public static Set<? extends UrlPermission> getLoginUserPermissions() {
        LoginUser user = getLoginUser();
        if (user != null) {
            return user.getPermissions();
        }
        return null;
    }

}
