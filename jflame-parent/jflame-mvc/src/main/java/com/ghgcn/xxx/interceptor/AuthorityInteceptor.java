package com.ghgcn.xxx.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jflame.toolkit.common.bean.CallResult;
import org.jflame.toolkit.common.bean.CallResult.ResultEnum;
import org.jflame.toolkit.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * url权限判断拦截器
 * 
 * @author yucan.zhang
 */
public class AuthorityInteceptor implements HandlerInterceptor {

    private final Logger log = LoggerFactory.getLogger(AuthorityInteceptor.class);
    private String fobidUrl;// 无权限转向页面地址，相对应用路径
    // @Resource
    // private AuthorityService authorityServiceImpl;
    private final String forbiddenJson = JsonHelper.toJson(new CallResult(ResultEnum.FORBIDDEN));

    public void setFobidUrl(String fobidUrl) {
        this.fobidUrl = fobidUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        boolean hasRight = true;
        if (contextPath != null && contextPath.length() > 0) {
            uri = uri.substring(contextPath.length());
        }
        // 可能出现/xx;jsessionid=jjj形式url，fcbw;号后参数
        int semicolonIndex = uri.lastIndexOf(";");
        if (semicolonIndex > -1) {
            uri = uri.substring(0, semicolonIndex);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 获取用户权限集合
            /* UserInfo loginUser = (UserInfo) request.getSession().getAttribute(WebConstant.SESSION_USER_KEY);
            // 判断权限
            if (!authorityServiceImpl.hasPermission(loginUser, uri)) {
                hasRight=false;
                log.warn("user:{} access no permission url:{}",loginUser.getUserId(),uri);
                if (WebUtils.isAjaxRequest(request)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    WebUtils.outJson(response, forbiddenJson);
                }else{
                    request.getRequestDispatcher(fobidUrl).forward(request, response);
                }
            }*/
        } else {
            hasRight = false;
        }

        return hasRight;
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
