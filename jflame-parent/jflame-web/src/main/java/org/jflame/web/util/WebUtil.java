package org.jflame.web.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.file.FileHelper;
import org.jflame.web.constants.WebConstant;

/**
 * web环境常用工具方法
 * 
 * @author yucan.zhang
 */
public class WebUtil {

    /**
     * 设置让浏览器弹出下载对话框的Header.
     * 
     * @param response HttpServletResponse
     * @param fileName 下载后的文件名.
     * @param fileSize 文件大小
     */
    public static void setFileDownloadHeader(HttpServletResponse response, String fileName, long fileSize) {
        String encodedfileName;
        try {
            encodedfileName = new String(fileName.getBytes("gbk"), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            encodedfileName = "download_file" + FileHelper.getExtension(fileName, true);
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        response.setContentType(WebConstant.MIME_TYPE_STREAM);
        response.setHeader("Content-Length", String.valueOf(fileSize));
    }

    /**
     * 设置客户端缓存过期时间 的Header.
     * 
     * @param response HttpServletResponse
     * @param expiresSeconds 过期时间,秒
     */
    public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
        response.setDateHeader("Expires", System.currentTimeMillis() + expiresSeconds * 1000);
        response.setHeader("Cache-Control", "max-age=" + expiresSeconds);
    }

    /**
     * 设置禁止客户端缓存的Header.
     * 
     * @param response HttpServletResponse
     */
    public static void setDisableCacheHeader(HttpServletResponse response) {
        response.setDateHeader("Expires", 1L);
        response.addHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
    }

    /**
     * 判断请求是否是一个ajax请求或返回要求是json数据.
     * <p>
     * 判断规则:1,请求头accepty含application/json 2,请求头x-requested-with=XMLHttpRequest.3,含参数mediaType=json
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String headAccept = request.getHeader("accept");
        boolean yes = false;
        if (headAccept != null && headAccept.indexOf(WebConstant.MIME_TYPE_JSON) >= 0) {
            yes = true;
        } else if (WebConstant.AJAX_REQUEST_FLAG.value()
                .equalsIgnoreCase(request.getHeader(WebConstant.AJAX_REQUEST_FLAG.name()))) {
            yes = true;
        }
        return yes;
    }

    /**
     * 输出json字符串.注:未设置输出编码
     * 
     * @param response HttpServletResponse
     * @param jsonStr json字符串
     * @throws IOException IOException
     */
    public static void outJson(HttpServletResponse response, String jsonStr) throws IOException {
        setDisableCacheHeader(response);
        response.setContentType(WebConstant.MIME_TYPE_JSON);
        PrintWriter out = response.getWriter();
        out.print(jsonStr);
        out.close();
    }

    volatile transient static String contextRootPath;

    /**
     * 获取应用的绝对url
     * <p>
     * 如:http://www.xx.com:8080/webapp,https://www.xx.com
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static String getApplicationPath(HttpServletRequest request) {
        if (contextRootPath == null) {
            contextRootPath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath();
        }
        return contextRootPath;
    }

    /**
     * 返回应用的绝对url,该值返回的是方法{@link #getApplicationPath(HttpServletRequest)}保存的静态变量，使用前请确保执行过此方法.
     * 
     * @return
     */
    public static String getApplicationPath() {
        return contextRootPath;
    }

    /**
     * 合并url，自动补充url分隔符/和纠正url
     * 
     * @param firstUrl 首个url，可以是绝对或相对路径,如果不以协议或/开头将补充/
     * @param relativeUrls 要合并的url，相对路径
     * @return
     */
    public static String mergeUrl(final String firstUrl, final String... relativeUrls) {
        String fullUrl = "";
        final char urlSplit = '/';
        if (relativeUrls.length == 0) {
            if (isAbsoluteUrl(firstUrl) && urlSplit != firstUrl.charAt(0)) {
                return firstUrl;
            } else {
                return urlSplit + fullUrl;
            }
        }
        for (String url : relativeUrls) {
            if (url.charAt(0) != urlSplit) {
                fullUrl += urlSplit;
            }
            fullUrl += url;
        }
        fullUrl = fullUrl.replace('\\', urlSplit).replaceAll("/{2,}", "/");

        if (firstUrl.charAt(firstUrl.length() - 1) == urlSplit) {
            fullUrl = firstUrl + fullUrl.substring(1);
        } else {
            fullUrl = firstUrl + fullUrl;
        }
        if (!isAbsoluteUrl(fullUrl) && urlSplit != fullUrl.charAt(0)) {
            fullUrl = urlSplit + fullUrl;
        }

        return fullUrl;
    }

    /**
     * 判断是否是绝对路径的url.
     * 
     * @param url url
     * @return
     */
    public static boolean isAbsoluteUrl(String url) {
        Pattern absouteUrlRegex = Pattern.compile("\\A[a-z0-9.+-]+://.*", Pattern.CASE_INSENSITIVE);
        return absouteUrlRegex.matcher(url).matches();
    }

    /**
     * 获取客户端ip地址.
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static String getRemoteClientIP(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.indexOf(",") > -1) {
            ip = ip.split(",")[0];
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        } else {
            ip = ip.trim();
        }
        return ip.trim();
    }

    /**
     * 获取指定名称的cookie值
     * 
     * @param request HttpServletRequest
     * @param cookieName cookie名
     * @return cookie字符串值，不存在返回null
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie c : cookies) {
                if (c.getName().equals(cookieName)) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 设置cookie
     * 
     * @param response HttpServletResponse
     * @param cookieName cookie名
     * @param cookieValue cookie值
     */
    public static void addCooke(HttpServletResponse response, String cookieName, String cookieValue) {
        response.addCookie(new Cookie(cookieName, cookieValue));
    }

    /**
     * 设置cookie，并设置生存时间
     * 
     * @param response HttpServletResponse
     * @param cookieName cookie名
     * @param cookieValue cookie值
     * @param maxAge 生存时间,单位秒，0将删除该cookie
     */
    public static void addCooke(HttpServletResponse response, String cookieName, String cookieValue, int maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
