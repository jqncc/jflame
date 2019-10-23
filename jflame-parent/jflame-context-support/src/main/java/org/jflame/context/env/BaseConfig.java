package org.jflame.context.env;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.context.filemanager.FileManagerFactory.FileManagerMode;
import org.jflame.toolkit.config.ConfigKey;
import org.jflame.toolkit.config.PropertiesConfigHolder;

/**
 * 提供项目通用配置获取的方法
 * 
 * @author yucan.zhang
 */
public abstract class BaseConfig {

    public final static ConfigKey<Boolean> CFG_DEBUGMODE = new ConfigKey<>("debugmode", false);
    public final static ConfigKey<String> CFG_APP_NO = new ConfigKey<String>("appNo");

    public final static ConfigKey<String> CFG_FILE_MODE = new ConfigKey<String>("file.manager", "local");
    public final static ConfigKey<String> CFG_SAVE_PATH = new ConfigKey<>("file.save.path");
    public final static ConfigKey<String> CFG_FILE_SERVER = new ConfigKey<>("file.server");
    public final static ConfigKey<String> CFG_IMAGE_SERVER = new ConfigKey<>("file.image.server");

    public final static ConfigKey<String[]> CFG_CORS_ALLOW_DOMAINS = new ConfigKey<>("cors.allowDomains",
            new String[] { "*" });
    public final static ConfigKey<String> CFG_CORS_ALLOW_METHODS = new ConfigKey<>("cors.allowMethods",
            "POST, GET,HEAD,OPTIONS");
    public final static ConfigKey<String> CFG_CORS_ADDHEADERS = new ConfigKey<>("cors.addHeaders");
    public final static ConfigKey<String[]> CFG_CORS_ALLOW_API = new ConfigKey<>("cors.allowed.api");
    public final static ConfigKey<String[]> CFG_CORS_DISALLOW_API = new ConfigKey<>("cors.disallowed.api");

    // ConfigKey<Long> CFG_CORS_MAXAGE = new ConfigKey<>("cors.maxAge", -1L);
    // ConfigKey<Boolean> CFG_CORS_ALLOW_CREDENTIALS = new ConfigKey<>("cors.allowCredentials", true);
    // ConfigKey<String> CFG_CORS_ALLOW_EXPOSEHEADERS = new ConfigKey<>("cors.exposeHeaders");

    public static String getString(ConfigKey<String> cfgKey) {
        return PropertiesConfigHolder.getString(cfgKey);
    }

    public static String[] getStringArray(ConfigKey<String[]> cfgKey) {
        return PropertiesConfigHolder.getStringArray(cfgKey);
    }

    /**
     * 系统当前是否是开发模式
     * 
     * @return
     */
    public static boolean isDebugMode() {
        return PropertiesConfigHolder.getBoolean(CFG_DEBUGMODE);
    }

    /**
     * 文件保存根路径,只在本地或NFS方式存储文件时有效
     * 
     * @return
     */
    public static String getFileSavePath() {
        return PropertiesConfigHolder.getString(CFG_SAVE_PATH);
    }

    public static String getFileServer() {
        return PropertiesConfigHolder.getString(CFG_FILE_SERVER);
    }

    /**
     * 图片服务器地址
     * 
     * @return
     */
    public static String getImgServer() {
        return PropertiesConfigHolder.getString(CFG_IMAGE_SERVER);
    }

    /**
     * 返回附件文件管理方式,当前支持未配置默认返回local
     * 
     * @return
     */
    public static FileManagerMode getFileManagerMode() {
        String fm = PropertiesConfigHolder.getString(CFG_FILE_MODE);
        return Enum.valueOf(FileManagerMode.class, fm.toLowerCase());
    }

    /**
     * cors跨域允许的地址Origins
     * 
     * @return
     */
    public static Set<String> corsAllowedOrigins() {
        String[] origins = PropertiesConfigHolder.getStringArray(CFG_CORS_ALLOW_DOMAINS);
        if (ArrayUtils.isNotEmpty(origins)) {
            return new HashSet<>(Arrays.asList(origins));
        }
        return Collections.emptySet();
    }

    /**
     * cors跨域允许的自定义请求头
     * 
     * @return
     */
    public static String corsAllowedHeader() {
        return PropertiesConfigHolder.getString(CFG_CORS_ADDHEADERS);
    }

    /**
     * 允许跨域访问的接口地址列表
     * 
     * @return
     */
    public static Set<String> corsAllowedUrls() {
        String[] allowedUrls = PropertiesConfigHolder.getStringArray(CFG_CORS_ALLOW_API);
        Set<String> set = new HashSet<>();
        if (ArrayUtils.isNotEmpty(allowedUrls)) {
            Collections.addAll(set, allowedUrls);
        }
        return set;
    }

    /**
     * 不允许跨域访问的接口地址列表
     * 
     * @return
     */
    public static Set<String> corsDisallowedUrls() {
        String[] disallowedUrls = PropertiesConfigHolder.getStringArray(CFG_CORS_DISALLOW_API);
        Set<String> set = new HashSet<>();
        if (ArrayUtils.isNotEmpty(disallowedUrls)) {
            Collections.addAll(set, disallowedUrls);
        }
        return set;
    }

    /**
     * 获取当前系统应用标识
     * 
     * @return
     */
    public static String getAppNo() {
        return PropertiesConfigHolder.getString(CFG_APP_NO);
    }

    /**
     * 获取配置参数alioss AccessId
     * 
     * @return
     */
    public static String getAliOSSAccessId() {
        return PropertiesConfigHolder.getString("file.alioss.accessId");
    }

    /**
     * 获取配置参数alioss AccessSecret
     * 
     * @return
     */
    public static String getAliOSSAccessSecret() {
        return PropertiesConfigHolder.getString("file.alioss.accessSecret");
    }

    /**
     * 获取配置参数alioss Bucket
     * 
     * @return
     */
    public static String getAliOSSBucket() {
        return PropertiesConfigHolder.getString("file.alioss.bucket");
    }

    /**
     * 获取fastdfs的配置文件名,默认值fastdfs.properties
     * 
     * @return
     */
    public static String getFastDFSConfigFile() {
        ConfigKey<String> key = new ConfigKey<>("file.fastdfs.configfile", "fastdfs.properties");
        return PropertiesConfigHolder.getString(key);
    }

}
