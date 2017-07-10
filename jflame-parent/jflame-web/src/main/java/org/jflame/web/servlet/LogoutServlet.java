package org.jflame.web.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.util.WebUtils;

@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {

    private String logoutPage = "/login.jsp";

    @Override
    public void init(ServletConfig config) throws ServletException {
        logoutPage = config.getInitParameter("logoutPage");
        if (StringHelper.isNotEmpty(logoutPage)) {
            logoutPage = logoutPage.trim();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        beforeLogout(request, resp);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        if (WebUtils.isAbsoluteUrl(logoutPage)) {
            resp.sendRedirect(logoutPage);
        } else {
            resp.sendRedirect(WebUtils.mergeUrl(request.getContextPath(), logoutPage));
        }
    }

    protected void beforeLogout(HttpServletRequest request, HttpServletResponse resp) {

    }
}
