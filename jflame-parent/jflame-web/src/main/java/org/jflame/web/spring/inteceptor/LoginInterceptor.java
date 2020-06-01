package org.jflame.web.spring.inteceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.jflame.commons.model.CallResult;
import org.jflame.commons.model.CallResult.ResultEnum;
import org.jflame.context.auth.model.LoginUser;
import org.jflame.web.WebUtils;
import org.jflame.web.spring.MyExceptionResolver;

/**
 * 登录拦截器
 * 
 * @author yucan.zhang
 */
public class LoginInterceptor implements HandlerInterceptor {

    private String loginUrl = "/login.jsp";
    private String userKey;
    private final CallResult<?> failed = new CallResult<>(ResultEnum.NO_AUTH);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        LoginUser loginUser = getLoginUser(req, resp);
        if (!authenticate(loginUser, req)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            if (MyExceptionResolver.isJsonResult(req, handler)) {
                WebUtils.outJson(resp, failed);
            } else {
                if (null != failed.getMessage()) {
                    req.setAttribute("errorMsg", failed.getMessage());
                }
                req.getRequestDispatcher(loginUrl)
                        .forward(req, resp);
            }
            return false;
        } else {
            onSuccess(loginUser, req, resp);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }

    /**
     * 设置登录用户缓存的key
     * 
     * @param userKey
     */
    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserKey() {
        return userKey == null ? WebUtils.SESSION_USER_KEY : userKey;
    }

    /**
     * 验证失败登录页地址
     * 
     * @param loginUrl
     */
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    /**
     * 登录验证成功后执行操作
     * 
     * @param loginUser 当前登录用户
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     */
    protected void onSuccess(LoginUser loginUser, HttpServletRequest req, HttpServletResponse resp) {

    }

    /**
     * 获取登录用户信息
     * 
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @return true有效
     */
    protected LoginUser getLoginUser(HttpServletRequest req, HttpServletResponse resp) {
        return (LoginUser) req.getSession()
                .getAttribute(getUserKey());
    }

    /**
     * 验证登录用户信息是否有效
     * 
     * @param loginUser
     * @param req
     * @return
     */
    protected boolean authenticate(LoginUser loginUser, HttpServletRequest req) {
        return loginUser != null;
    }

    public CallResult<?> getFailed() {
        return failed;
    }

}
