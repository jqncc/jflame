package org.jflame.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.config.BaseParamStrategy;
import org.jflame.commons.config.FilterParamConfig;

/**
 * 继承该类的过滤器每个请求只被过滤一次.部分容器或servlet版本会导致请求被过滤多次
 * 
 * @author yucan.zhang
 */
public abstract class OncePerRequestFilter implements Filter {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected String alreadyFilteredFlag;
    protected BaseParamStrategy filterParam;
    protected static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";

    @Override
    public final void init(FilterConfig filterConfig) throws ServletException {
        String name = filterConfig.getFilterName();
        if (name == null) {
            name = getClass().getName();
        }
        alreadyFilteredFlag = name + ALREADY_FILTERED_SUFFIX;
        initParamStrategy(filterConfig);
        internalInit(filterConfig);
    }

    private void initParamStrategy(FilterConfig filterConfig) {
        filterParam = new FilterParamConfig(filterConfig);
    }

    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getAttribute(alreadyFilteredFlag) != null) {
            log.trace("Filter '{}' already executed.  Proceeding without invoking this filter.", alreadyFilteredFlag);
            filterChain.doFilter(request, response);
            return;
        } else {
            log.trace("Filter '{}' not yet executed.  Executing now.", alreadyFilteredFlag);
            request.setAttribute(alreadyFilteredFlag, Boolean.TRUE);
            try {
                doFilterInternal(request, response, filterChain);
            } finally {
                request.removeAttribute(alreadyFilteredFlag);
            }
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * 子类实际执行的拦截操作
     *
     * @param request incoming {@code ServletRequest}
     * @param response outgoing {@code ServletResponse}
     * @param chain the {@code FilterChain} to execute
     * @throws ServletException if there is a problem processing the request
     * @throws IOException if there is an I/O problem processing the request
     */
    protected abstract void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException;

    protected void internalInit(FilterConfig filterConfig) {
    };

}
