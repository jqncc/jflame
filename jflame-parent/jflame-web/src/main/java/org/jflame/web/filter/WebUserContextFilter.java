package org.jflame.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jflame.context.auth.context.UserContext;
import org.jflame.context.auth.context.UserContextHolder;
import org.jflame.web.WebUtils;

/**
 * 登录用户上下文绑定到线程Filter
 * 
 * @author yucan.zhang
 */
public class WebUserContextFilter extends IgnoreUrlMatchFilter {

    @Override
    protected void doInternalFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object context = session.getAttribute(WebUtils.SESSION_USER_KEY);
            if (context != null && context instanceof UserContext) {
                if (log.isDebugEnabled()) {
                    log.debug("UserContext holder");
                }
                try {
                    UserContextHolder.setContext((UserContext) context);
                    chain.doFilter(req, res);
                } finally {
                    UserContextHolder.clearContext();
                }
            } else {
                chain.doFilter(req, res);
            }
        } else {
            chain.doFilter(req, res);
        }
    }
}
