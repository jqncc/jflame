package org.jflame.context.web.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jflame.commons.common.bean.CallResult;
import org.jflame.commons.common.bean.CallResult.ResultEnum;
import org.jflame.commons.config.ConfigKey;
import org.jflame.commons.config.ServletParamConfig;
import org.jflame.commons.util.JsonHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.util.UrlHelper;
import org.jflame.context.web.WebUtils;

/**
 * 通用登录注销servlet
 * 
 * @author yucan.zhang
 */
@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {

    private final ConfigKey<String> LOGOUT_PAGE_KEY = new ConfigKey<>("logoutPage");// 注销后跳转页面
    private final ConfigKey<String> LOGOUT_JSON_KEY = new ConfigKey<>("logoutJson");// 注销后返回的json消息,ajax请求时使用
    private String logoutPage;
    private String logoutJson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletParamConfig servletParam = new ServletParamConfig(config);
        logoutPage = servletParam.getString(LOGOUT_PAGE_KEY);
        logoutJson = servletParam.getString(LOGOUT_JSON_KEY);
        if (StringHelper.isEmpty(logoutJson)) {
            logoutJson = JsonHelper.toJson(new CallResult<>(ResultEnum.SUCCESS.getStatus(), "登出成功"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        beforeLogout(request, resp);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        if (WebUtils.isAjaxRequest(request)) {
            WebUtils.outJson(resp, logoutJson);
        } else {
            forward(request, resp);
        }
    }

    protected void beforeLogout(HttpServletRequest request, HttpServletResponse resp)
            throws ServletException, IOException {

    }

    protected void forward(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        if (UrlHelper.isAbsoluteUrl(logoutPage)) {
            resp.sendRedirect(logoutPage);
        } else {
            String sitePath = WebUtils.getApplicationPath(request);
            if (logoutPage != null) {
                sitePath = UrlHelper.mergeUrl(sitePath, logoutPage);
            }
            resp.sendRedirect(sitePath);
        }
    }

    public String getLogoutPage() {
        return logoutPage;
    }

    public void setLogoutPage(String logoutPage) {
        this.logoutPage = logoutPage;
    }

    public String getLogoutJson() {
        return logoutJson;
    }

    public void setLogoutJson(String logoutJson) {
        this.logoutJson = logoutJson;
    }

}
