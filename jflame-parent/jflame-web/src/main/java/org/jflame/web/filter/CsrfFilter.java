package org.jflame.web.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.reflect.SpiFactory;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.config.ISysConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * csrf攻击拦截，比对请求来源referer.
 * <p>
 * 配置参数:<br>
 * whiteFile[可选] 白名单文件名,只在classes目录下查找.<br>
 * errorPage[可选] 错误转向页面,不设置将返回400错误请求<br>
 * 
 * @author yucan.zhang
 */
public class CsrfFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);
    private final String CSRF_WHITEFILE_CONFIGKEY = "csrf.whitefile";
    private final String CSRF_ERRORPAGE_CONFIGKEY = "csrf.errorpage";
    // 白名单
    private List<URI> whiteUrls;
    private String errorPage;

    public void init(FilterConfig filterConfig) {
        ISysConfig sysConfig = SpiFactory.getSingleBean(ISysConfig.class);
        if (sysConfig != null) {
            String paramWhiteFile = sysConfig.getTextParam(CSRF_WHITEFILE_CONFIGKEY);
            String paramErrorPage = sysConfig.getTextParam(CSRF_ERRORPAGE_CONFIGKEY);
            if (StringHelper.isNotEmpty(paramErrorPage)) {
                errorPage = paramErrorPage.trim();
            }
            if (StringHelper.isNotEmpty(paramWhiteFile)) {
                if (paramWhiteFile.charAt(0)!=FileHelper.UNIX_SEPARATOR) {
                    paramWhiteFile=FileHelper.UNIX_SEPARATOR+paramWhiteFile;
                }
                List<String> whiteUrlStrs = null;
                try {
                    Path p = Paths.get(CsrfFilter.class.getResource(paramWhiteFile.trim()).toURI());
                    whiteUrlStrs = Files.readAllLines(p, Charset.forName(CharsetHelper.UTF_8));
                } catch (IOException | URISyntaxException e) {
                    logger.error("csrf白名单读取失败", e);
                }
                if (CollectionHelper.isNotEmpty(whiteUrlStrs)) {
                    for (String urlStr : whiteUrlStrs) {
                        if (StringUtils.isNotBlank(urlStr)) {
                            try {
                                URI.create(urlStr.trim());
                            } catch (IllegalArgumentException e) {
                                logger.error("不是正确的URI地址" + urlStr, e);
                            }
                        }
                    }
                }
            }
        } else {
            logger.error("未找到ISysConfig实现类");
        }
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        // 获取请求url地址
        String referurl = request.getHeader("Referer");
        logger.debug("crsf check referurl:{}", referurl);
        if (isWhiteReq(referurl, request)) {
            chain.doFilter(request, response);
        } else {
            String url = request.getRequestURL().toString();
            logger.warn("非法请求来源:url={},referer={}", url, referurl);
            if (StringHelper.isNotEmpty(errorPage)) {
                request.getRequestDispatcher(errorPage).forward(request, response);
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
        logger.debug("crsf filter referurl:{}", referUrl);
        boolean isSafeUri = false;
        if (StringHelper.isEmpty(referUrl)) {
            isSafeUri = true;
        } else {
            URI refererUri = URI.create(referUrl);
            // 与当前应用和白名单地址比较协议主机端口
            if (refererUri.getScheme().equals(request.getScheme())
                    && refererUri.getHost().equals(request.getServerName())
                    && refererUri.getPort() == request.getServerPort()) {
                isSafeUri = true;
            } else {
                for (URI uri : whiteUrls) {
                    if (uri.getScheme().equals(refererUri.getScheme())
                            && uri.getAuthority().equals(refererUri.getAuthority())) {
                        isSafeUri = true;
                        break;
                    }
                }
            }
        }

        return isSafeUri;
    }

    public void destroy() {
    }

}
