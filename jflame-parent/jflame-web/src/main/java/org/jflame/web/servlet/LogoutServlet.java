package org.jflame.web.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jflame.toolkit.common.bean.CallResult;
import org.jflame.toolkit.common.bean.CallResult.ResultEnum;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.config.DefaultConfigKeys;
import org.jflame.web.config.ServletParamConfig;
import org.jflame.web.util.WebUtils;

@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {

    private String logoutPage;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletParamConfig servletParam = new ServletParamConfig(config);
        logoutPage = servletParam.getString(DefaultConfigKeys.LOGOUT_PAGE);
        if (StringHelper.isNotEmpty(logoutPage)) {
            logoutPage = logoutPage.trim();
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        beforeLogout(request, resp);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        if (WebUtils.isAjaxRequest(request)) {
            WebUtils.outJson(resp, new CallResult(ResultEnum.SUCCESS));
        } else {
            forward(request, resp);
        }
    }

    protected void beforeLogout(HttpServletRequest request, HttpServletResponse resp)
            throws ServletException, IOException {

    }

    protected void forward(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        if (WebUtils.isAbsoluteUrl(logoutPage)) {
            resp.sendRedirect(logoutPage);
        } else {
            resp.sendRedirect(WebUtils.mergeUrl(request.getContextPath(), logoutPage));
        }
    }

    public String getLogoutPage() {
        return logoutPage;
    }

    public void setLogoutPage(String logoutPage) {
        this.logoutPage = logoutPage;
    }

}
