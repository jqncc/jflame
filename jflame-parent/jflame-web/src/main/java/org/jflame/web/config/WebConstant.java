package org.jflame.web.config;

import org.apache.commons.lang3.ArrayUtils;
import org.jflame.toolkit.common.bean.pair.NameValuePair;
import org.jflame.toolkit.util.EnumHelper;

/**
 * web项目常量定义
 * 
 * @author yucan.zhang
 */
public final class WebConstant {

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
        png("image/png"), jpg("image/jpeg"), jpeg("image/jpeg"), gif("image/gif"), bmp("application/x-bmp"), ico(
                "image/x-icon");

        private String mime;

        public String getMime() {
            return mime;
        }

        private MimeImages(String mime) {
            this.mime = mime;
        }

        public static boolean support(String mimeType) {
            for (MimeImages mn : MimeImages.values()) {
                if (mn.name().equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * web图片类型扩展名
     */
    public final static String[] imageExts = EnumHelper.enumNames(MimeImages.class);
    /**
     * web静态资源扩展名
     */
    public final static String[] webStaticExts = ArrayUtils.addAll(WebConstant.imageExts, "js", "css", "ttf", "tiff",
            "font");
    /**
     * 当前登录用户在session中的key
     */
    @Deprecated
    public final static String SESSION_USER_KEY = "current_user";

}
