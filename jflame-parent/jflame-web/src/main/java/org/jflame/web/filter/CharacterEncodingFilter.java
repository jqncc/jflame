package org.jflame.web.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jflame.commons.config.ConfigKey;

/**
 * 字符编码转换过滤器
 * 
 * @author yucan.zhang
 */
public class CharacterEncodingFilter extends OncePerRequestFilter {

    private final ConfigKey<String> CHARSET_ENCODE_ENCODING = new ConfigKey<>("encoding",
            StandardCharsets.UTF_8.name());
    private final ConfigKey<Boolean> CHARSET_ENCODE_FORCE = new ConfigKey<>("forceEncoding", false);
    private String encoding;
    private boolean forceEncoding = false;

    @Override
    protected void internalInit(FilterConfig filterConfig) {
        encoding = filterParam.getString(CHARSET_ENCODE_ENCODING);
        forceEncoding = filterParam.getBoolean(CHARSET_ENCODE_FORCE);
    }

    @Override
    protected void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (this.encoding != null && (this.forceEncoding || request.getCharacterEncoding() == null)) {
            request.setCharacterEncoding(this.encoding);
            if (this.forceEncoding) {
                response.setCharacterEncoding(this.encoding);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
