package org.jflame.web.spring.inteceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.commons.util.StringHelper;
import org.jflame.context.auth.context.UserContextHolder;
import org.jflame.context.auth.model.LoginUser;
import org.jflame.web.WebUtils;
import org.jflame.web.spring.SpringWebUtils;

/**
 * url权限判断拦截器,应配置于登录拦截器之后
 * 
 * @author yucan.zhang
 */
public class AuthorityInteceptor implements HandlerInterceptor {

    private final Logger log = LoggerFactory.getLogger(AuthorityInteceptor.class);
    private String fobidUrl = "/403.html";// 无权限转向页面地址，相对应用路径
    private final CallResult<?> errJson = new CallResult<>(ResultEnum.FORBIDDEN);
    private String superAdminRole = null;// 设置超级管理员的角色标识,如果设置了值表示启用超管角色无需验证权限

    public void setFobidUrl(String fobidUrl) {
        this.fobidUrl = fobidUrl;
    }

    public void setSuperAdminRole(String superAdminRoleCode) {
        this.superAdminRole = superAdminRoleCode;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
        String uri = WebUtils.getRequestPath(request);
        if (log.isDebugEnabled()) {
            log.debug("auth filter,uri:{}", uri);
        }
        // 获取用户
        LoginUser curUser = UserContextHolder.getContext()
                .getUser();
        if (StringHelper.isNotEmpty(superAdminRole) && curUser.hasRole(superAdminRole)) {
            return true;
        }
        // 判断权限
        if (!curUser.hasRight(uri)) {
            log.warn("access no permission url:{}", uri);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (SpringWebUtils.isJsonResult(request, arg2)) {
                WebUtils.outJson(response, errJson);
            } else {
                request.getRequestDispatcher(fobidUrl)
                        .forward(request, response);
            }
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
            throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
            throws Exception {
    }

}
