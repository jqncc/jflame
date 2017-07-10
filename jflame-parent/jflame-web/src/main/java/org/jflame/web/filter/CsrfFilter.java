package org.jflame.web.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.config.WebConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * csrf攻击拦截，比对请求来源referer.
 * <p>
 * 配置参数:<br>
 * whiteFile[可选] 白名单文件名,只在classes目录下查找.<br>
 * errorPage[可选] 错误转向页面,默认返回400错误请求<br>
 * ignoreStatic[可选] 是否忽略静态资源文件,默认为true<br>
 * 
 * @author yucan.zhang
 */
public class CsrfFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);

    private List<URI> whiteUrls; // 白名单
    private String errorPage;//错误转向页面
    private boolean isIgnoreStatic=true;//是否忽略静态资源文件

    public void init(FilterConfig filterConfig) {
        final String WHITEFILE_PARAM = "whitefile";
        final String ERRORPAGE_PARAM = "errorpage";
        final String IGNORE_PARAM = "ignoreStatic";

        String whiteFile = filterConfig.getInitParameter(WHITEFILE_PARAM);
        String errPage = filterConfig.getInitParameter(ERRORPAGE_PARAM);
        
        if(StringHelper.isNotEmpty(filterConfig.getInitParameter(IGNORE_PARAM))){
            isIgnoreStatic =Boolean.parseBoolean(filterConfig.getInitParameter(IGNORE_PARAM).trim());
        }
        if (StringHelper.isNotEmpty(errPage)) {
            errorPage = errPage.trim();
        }
        if (StringHelper.isNotEmpty(whiteFile)) {
            List<String> whiteUrlStrs = null;
            try {
                URL url = CsrfFilter.class.getResource(whiteFile.trim());
                if (url != null) {
                    Path whiteFilePath = Paths.get(url.toURI());
                    if (Files.exists(whiteFilePath)) {
                        whiteUrlStrs = Files.readAllLines(whiteFilePath, Charset.forName(CharsetHelper.UTF_8));
                    } else {
                        logger.error("csrf白名单文件路径不存在或不可读{}", whiteFile);
                    }
                } else {
                    logger.error("csrf白名单文件路径不正确{}", whiteFile);
                }
            } catch (IOException | URISyntaxException e) {
                logger.error("csrf白名单读取失败", e);
            }
            if (CollectionHelper.isNotEmpty(whiteUrlStrs)) {
                whiteUrls = new ArrayList<>();
                for (String urlStr : whiteUrlStrs) {
                    if (StringUtils.isNotBlank(urlStr)) {
                        try {
                            whiteUrls.add(URI.create(urlStr.trim()));
                        } catch (IllegalArgumentException e) {
                            logger.error("不是正确的URI地址" + urlStr, e);
                        }
                    }
                }
            }
        }

    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        //忽略静态文件地址
        if (isIgnoreStatic&&isWebStatic(request.getPathInfo())) {
            chain.doFilter(request, response);
        }
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
        boolean isSafeUri = false;
        if (StringHelper.isEmpty(referUrl)) {
            isSafeUri = true;
        } else {
            URI refererUri = URI.create(referUrl);
            //logger.debug(refererUri.getHost()+"="+request.getServerName());
            // 与当前应用和白名单地址比较协议主机端口
            if (refererUri.getScheme().equals(request.getScheme())
                    && refererUri.getHost().equals(request.getServerName())
                    && refererUri.getPort() == request.getServerPort()) {
                isSafeUri = true;
            } else {
                if (CollectionHelper.isNotEmpty(whiteUrls)) {
                    for (URI uri : whiteUrls) {
                        if (refererUri.getScheme().equals(uri.getScheme())
                                && uri.getAuthority().equals(refererUri.getAuthority())) {
                            isSafeUri = true;
                            break;
                        }
                    }
                }
            }
        }

        return isSafeUri;
    }
    
    /**
     * 判断是否是web静态文件
     * @param requestUrl 请求路径
     * @return
     */
    private boolean isWebStatic(String requestUrl){
        if (requestUrl==null) {
            return false;
        }
        String ext=FileHelper.getExtension(requestUrl,false);
        return ArrayUtils.contains(WebConstant.webStaticExts,ext);
    }
    
    public void destroy() {
    }

}
