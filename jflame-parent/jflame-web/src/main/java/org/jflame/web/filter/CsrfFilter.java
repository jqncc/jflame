package org.jflame.web.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jflame.commons.config.ConfigKey;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.commons.valid.ValidatorHelper;
import org.jflame.web.WebUtils;

/**
 * csrf攻击拦截，比对请求来源referer.
 * <p>
 * 配置参数(同时继承IgnoreUrlMatchFilter参数):<br>
 * 1.whiteFile[可选] 白名单文件名,只在classpath目录下查找.<br>
 * 2.errorPage[可选] 错误转向页面,默认返回400错误请求<br>
 * 
 * @author yucan.zhang
 */
public class CsrfFilter extends IgnoreUrlMatchFilter {

    private final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);

    /**
     * CsrfFilter参数,验证失败中转页面
     */
    ConfigKey<String> CSRF_ERROR_PAGE = new ConfigKey<>("csrf.errorpage");
    /**
     * CsrfFilter参数,白名单文件路径
     */
    ConfigKey<String> CSRF_WHITE_FILE = new ConfigKey<>("csrf.whitefile");

    private List<String> whiteUrls; // 白名单
    private String errorPage;// 错误转向页面
    private final String headerReferer = "Referer";

    protected final void doInternalFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        // 获取请求url地址
        String referurl = request.getHeader(headerReferer);
        if (isWhiteReq(referurl, request)) {
            chain.doFilter(request, response);
        } else {
            String url = request.getRequestURL()
                    .toString();
            logger.warn("非法请求来源:ip={},url={},referer={}", WebUtils.getRemoteClientIP(request), url, referurl);
            if (StringHelper.isNotEmpty(errorPage)) {
                request.getRequestDispatcher(errorPage)
                        .forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "非法请求来源");
            }
            return;
        }
    }

    /*
     * 判断是否是白名单
     */
    private boolean isWhiteReq(String referUrl, HttpServletRequest request) {
        boolean isSafeUri = false;
        if (StringHelper.isEmpty(referUrl)) {
            isSafeUri = true;
        } else {
            URI refererUri = URI.create(referUrl);
            // 只比较域名
            if (refererUri.getHost()
                    .equals(request.getServerName())) {
                isSafeUri = true;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("referer uri,host={},port={}", refererUri.getHost(), refererUri.getPort());
                    logger.debug("request uri,host={},port={}", request.getServerName(), request.getServerPort());
                }
                if (CollectionHelper.isNotEmpty(whiteUrls)) {
                    for (String urlRegex : whiteUrls) {
                        if (ValidatorHelper.regex(referUrl, urlRegex)) {
                            isSafeUri = true;
                            break;
                        }
                    }
                }
            }
        }

        return isSafeUri;
    }

    @Override
    protected void doInternalInit(FilterConfig filterConfig) {
        String whiteFile = filterParam.getString(CSRF_WHITE_FILE);
        errorPage = filterParam.getString(CSRF_ERROR_PAGE);

        if (StringHelper.isNotEmpty(whiteFile)) {
            try {
                URL url = CsrfFilter.class.getResource(whiteFile.trim());
                if (url != null) {
                    Path whiteFilePath = Paths.get(url.toURI());
                    if (Files.exists(whiteFilePath)) {
                        whiteUrls = Files.readAllLines(whiteFilePath, StandardCharsets.UTF_8);
                    } else {
                        logger.error("csrf白名单文件路径不存在或不可读{}", whiteFile);
                    }
                } else {
                    logger.error("csrf白名单文件路径不正确{}", whiteFile);
                }
            } catch (IOException | URISyntaxException e) {
                logger.error("csrf白名单读取失败", e);
            }
        }
    }

    @Override
    public void destroy() {
    }

}
