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

/**
 * Filter,调用前登录用户上下文绑定到线程,调用后如果
 * 
 * @author yucan.zhang
 */
public class WebUserContextFilter extends IgnoreUrlMatchFilter {

    @Override
    protected void doInternalFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(false);
        UserContext ctxBeforeExecute = null;
        try {
            if (session != null) {
                Object context = session.getAttribute(UserContext.CONTEXT_KEY);
                if (context != null && context instanceof UserContext) {
                    ctxBeforeExecute = (UserContext) context;
                    UserContextHolder.setContext(ctxBeforeExecute);
                    if (log.isDebugEnabled()) {
                        log.debug("read userContext from session:{}", UserContextHolder.getContext()
                                .getUser()
                                .getUserName());
                    }
                    chain.doFilter(req, res);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("session userContext not found");
                    }
                    chain.doFilter(req, res);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("session null");
                }
                chain.doFilter(req, res);
            }
        } finally {
            UserContextHolder.clearContext();
        }
    }
}
