package org.jflame.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jflame.toolkit.config.ConfigKey;

public class CharacterEncodingFilter extends OncePerRequestFilter {

    private String encoding;
    private boolean forceEncoding = false;
    private final ConfigKey<String> CHARSET_ENCODE_ENCODING = new ConfigKey<>("encoding", "utf-8");
    private final ConfigKey<Boolean> CHARSET_ENCODE_FORCE = new ConfigKey<>("forceEncoding", false);

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
    protected void internalInit(FilterConfig filterConfig) {
        encoding = filterParam.getString(CHARSET_ENCODE_ENCODING);
        forceEncoding = filterParam.getBoolean(CHARSET_ENCODE_FORCE);
    }

    @Override
    public void destroy() {
    }

}
