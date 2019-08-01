package org.jflame.web.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.common.bean.pair.NameValuePair;
import org.jflame.toolkit.excel.ExcelCreator;
import org.jflame.toolkit.excel.IExcelEntity;
import org.jflame.toolkit.net.IPAddressHelper;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.EnumHelper;
import org.jflame.toolkit.util.JsonHelper;
import org.jflame.toolkit.util.StringHelper;

import org.apache.commons.lang3.ArrayUtils;

/**
 * web环境常用常量定义和工具方法
 * 
 * @author yucan.zhang
 */
public class WebUtils {

    /**
     * http mine,json类型
     */
    public static final String MIME_TYPE_JSON = "application/json";
    /**
     * http mine,json类型指定utf-8编码
     */
    public final static String MIME_TYPE_JSON_UTF8 = MIME_TYPE_JSON + ";charset=UTF-8";
    /**
     * http mine,excel类型
     */
    public static final String MIME_TYPE_EXCEL = "application/vnd.ms-excel";
    /**
     * post请求的content-type
     */
    public final static String MIME_TYPE_POST = "application/x-www-form-urlencoded";
    /**
     * http mine,二进制,常用于下载文件
     */
    public final static String MIME_TYPE_STREAM = "applicatoin/octet-stream";
    /**
     * ajax请求头标识
     */
    public final static NameValuePair AJAX_REQUEST_FLAG = new NameValuePair("x-requested-with", "XMLHttpRequest");

    /**
     * web支持图片格式枚举
     * 
     * @author yucan.zhang
     */
    public enum MimeImages {
        png("image/png"),
        jpg("image/jpeg"),
        jpeg("image/jpeg"),
        gif("image/gif"),
        bmp("application/x-bmp"),
        ico("image/x-icon");

        private String mime;

        public String getMime() {
            return mime;
        }

        private MimeImages(String mime) {
            this.mime = mime;
        }

        public static boolean support(String mimeType) {
            for (MimeImages mn : MimeImages.values()) {
                if (mn.name()
                        .equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * web图片类型扩展名
     */
    public final static String[] IMAGE_EXTS = EnumHelper.enumNames(MimeImages.class);
    /**
     * web静态资源扩展名
     */
    public final static String[] WEB_STATIC_EXTS = ArrayUtils.addAll(IMAGE_EXTS, "js", "css", "ttf", "tiff", "font");
    /**
     * 当前登录用户在session中的key
     */
    public final static String SESSION_USER_KEY = "current_user";

    /**
     * 设置让浏览器弹出下载对话框的Header.
     * 
     * @param response HttpServletResponse
     * @param fileName 下载后的文件名.
     * @param fileSize 文件大小
     */
    public static void setFileDownloadHeader(HttpServletResponse response, String fileName, long fileSize) {
        String encodedfileName = CharsetHelper.reEncodeGBK(fileName);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        response.setContentType(MIME_TYPE_STREAM);
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
     * 判断请求是否是一个ajax请求
     * <p>
     * 请求头含x-requested-with=XMLHttpRequest
     * 
     * @param request HttpServletRequest
     * @return true=是ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (AJAX_REQUEST_FLAG.value()
                .equalsIgnoreCase(request.getHeader(AJAX_REQUEST_FLAG.name()))) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否请求json格式数据,header accept带json或请求路径以json结尾
     * 
     * @param request
     * @return true=是json请求
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        String headAccept = request.getHeader("accept");
        String flag = "json";
        boolean yes = false;
        if (headAccept != null && headAccept.indexOf(flag) >= 0) {
            yes = true;
        } else if (request.getServletPath()
                .endsWith(flag)) {
            yes = true;
        }
        return yes;
    }

    public static <T extends Serializable> void outJson(HttpServletResponse response, T entity) throws IOException {
        outJson(response, entity, null);
    }

    /**
     * 对象转为json后输出到到response.
     * 
     * @param response HttpServletResponse
     * @param entity 待序列化对象
     * @param charset 字符集,为null时采用utf-8
     * @throws IOException
     */
    public static <T extends Serializable> void outJson(HttpServletResponse response, T entity, Charset charset)
            throws IOException {
        setDisableCacheHeader(response);
        if (charset == null) {
            response.setContentType(MIME_TYPE_JSON_UTF8);
        } else {
            response.setContentType(MIME_TYPE_JSON + ";charset=" + charset.name());
        }
        PrintWriter out = response.getWriter();
        if (entity instanceof String) {
            out.print(entity);
        } else {
            out.print(JsonHelper.toJson(entity));
        }
    }

    /**
     * 获取应用的绝对url
     * <p>
     * 如:http://www.xx.com:8080/webapp,https://www.xx.com
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static String getApplicationPath(HttpServletRequest request) {
        String tmpPath = request.getScheme() + "://" + request.getServerName();
        // 80,443不显示端口号
        if (request.getServerPort() != 80 && !(request.getServerPort() == 443 && "https".equals(request.getScheme()))) {
            tmpPath = tmpPath + ":" + request.getServerPort();
        }
        return tmpPath + request.getContextPath();
    }

    /**
     * 返回请求路径,不包含应用路径和查询参数
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();
        if (request.getPathInfo() != null) {
            url += request.getPathInfo();
        }
        return url;
    }

    /**
     * 合并url，自动补充url分隔符/和纠正url.<b>不适合文件系统路径合并</b>
     * 
     * @param firstUrl 首个url，可以是绝对或相对路径,如果不以协议或/开头将补充/
     * @param relativeUrls 要合并的url，相对路径
     * @return
     */
    @Deprecated
    public static String mergeUrl(final String firstUrl, final String... relativeUrls) {
        if (firstUrl == null) {
            throw new IllegalArgumentException("argument 'firstUrl' must not be null");
        }
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
        fullUrl = fullUrl.replace('\\', urlSplit)
                .replaceAll("/{2,}", "/");
        if (firstUrl.length() > 1 && firstUrl.charAt(firstUrl.length() - 1) == urlSplit) {
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
    @Deprecated
    public static boolean isAbsoluteUrl(String url) {
        if (StringHelper.isEmpty(url)) {
            return false;
        }
        return URI.create(url)
                .isAbsolute();
    }

    private static String[] localips = { "127.0.0.1","0:0:0:0:0:0:0:1" };

    /**
     * 获取客户端ip地址
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static String getRemoteClientIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (isDisabledIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isDisabledIp(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (isDisabledIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isDisabledIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (isDisabledIp(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个ip取第一个
        if (ip.indexOf(",") >= 0) {
            String[] ips = ip.split(",");
            for (String addr : ips) {
                if (!isDisabledIp(addr)) {
                    ip = addr;
                    break;
                }
            }
        }
        ip = ip.trim();
        if (ArrayUtils.contains(localips, ip)) {
            ip = IPAddressHelper.getLocalIP();
        }
        return ip;
    }

    private static boolean isDisabledIp(String ip) {
        if (StringHelper.isNotEmpty(ip)) {
            ip = ip.trim();
        } else {
            return true;
        }
        return localips[0].equals(ip) || localips[1].equals(ip) || "unknown".equalsIgnoreCase(ip);
    }

    /**
     * 导出excel文件到浏览器
     * 
     * @param data 数据集
     * @param fileName 文件名,浏览器要显示的文件名
     * @param response
     * @throws IOException
     */
    @Deprecated
    public static void exportExcel(final List<? extends IExcelEntity> data, String fileName,
            HttpServletResponse response) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCreator.export(data, out);
        setFileDownloadHeader(response, fileName, out.size());
        out.writeTo(response.getOutputStream());
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
                if (c.getName()
                        .equals(cookieName)) {
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
