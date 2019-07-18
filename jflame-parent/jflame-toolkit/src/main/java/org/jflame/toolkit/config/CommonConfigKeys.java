package org.jflame.toolkit.config;

/**
 * 内置常用配置键
 * 
 * @author yucan.zhang
 */
public interface CommonConfigKeys {

    /**
     * 配置文件路径在ServletContext(web.xml)参数名
     */
    public final static String CONFIG_FILE_KEY = "configFile";

    /**
     * 是否debug模式
     */
    ConfigKey<Boolean> DEBUGMODE = new ConfigKey<>("debugmode", false);

    /**
     * 上传文件保存路径,默认值/upload
     */
    ConfigKey<String> SAVE_PATH = new ConfigKey<>("save.path");
    /**
     * 图片读取或保存路径
     */
    @Deprecated
    ConfigKey<String> IMAGE_SAVE_PATH = new ConfigKey<>("image.save.path");

    ConfigKey<String> IMAGE_SERVER = new ConfigKey<>("image.server");

    ConfigKey<String[]> CORS_ALLOW_DOMAINS = new ConfigKey<>("cors.allowDomains", new String[] { "*" });
    ConfigKey<String> CORS_ALLOW_METHODS = new ConfigKey<>("cors.allowMethods", "POST, GET,HEAD,OPTIONS");
    ConfigKey<String> CORS_ADDHEADERS = new ConfigKey<>("cors.addHeaders");
    ConfigKey<Long> CORS_MAXAGE = new ConfigKey<>("cors.maxAge", -1L);
    ConfigKey<Boolean> CORS_ALLOW_CREDENTIALS = new ConfigKey<>("cors.allowCredentials", true);
    ConfigKey<String> CORS_ALLOW_EXPOSEHEADERS = new ConfigKey<>("cors.exposeHeaders");
    ConfigKey<String[]> CORS_ALLOW_API = new ConfigKey<>("cors.allowed.api");
    ConfigKey<String[]> CORS_DISALLOW_API = new ConfigKey<>("cors.disallowed.api");

}
