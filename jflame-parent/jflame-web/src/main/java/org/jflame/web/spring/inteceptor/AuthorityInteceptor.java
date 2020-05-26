package org.jflame.web.spring.inteceptor;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.jflame.commons.common.bean.CallResult;
import org.jflame.commons.common.bean.CallResult.ResultEnum;
import org.jflame.context.auth.AuthorityUtils;
import org.jflame.context.auth.model.UrlPermission;
import org.jflame.web.WebUtils;
import org.jflame.web.spring.MyExceptionResolver;
import org.jflame.web.spring.WebContextHolder;

/**
 * url权限判断拦截器,应配置于登录拦截器之后
 * 
 * @author yucan.zhang
 */
public class AuthorityInteceptor implements HandlerInterceptor {

    private final Logger log = LoggerFactory.getLogger(AuthorityInteceptor.class);
    private String fobidUrl = "/403.html";// 无权限转向页面地址，相对应用路径
    private final CallResult<?> errJson = new CallResult<>(ResultEnum.FORBIDDEN);

    public void setFobidUrl(String fobidUrl) {
        this.fobidUrl = fobidUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
        String uri = WebUtils.getRequestPath(request);
        if (log.isDebugEnabled()) {
            log.debug("auth filter,uri:{}", uri);
        }
        // 获取用户权限集合
        Set<? extends UrlPermission> funList = WebContextHolder.getLoginUserPermissions();
        // 判断权限
        if (!AuthorityUtils.hasPermissionByUrl(funList, uri)) {
            log.warn("access no permission url:{}", uri);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (MyExceptionResolver.isJsonResult(request, arg2)) {
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