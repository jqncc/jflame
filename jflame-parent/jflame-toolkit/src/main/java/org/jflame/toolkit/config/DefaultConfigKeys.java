package org.jflame.toolkit.config;

/**
 * 内置常用配置键
 * 
 * @author yucan.zhang
 */
public interface DefaultConfigKeys {

    /**
     * 上传文件保存路径,默认值/upload
     */
    ConfigKey<String> SAVE_PATH = new ConfigKey<>("save.path");
    /**
     * 图片保存路径,默认值/upload/images
     */
    ConfigKey<String> IMAGE_SAVE_PATH = new ConfigKey<>("image.save.path");
    /**
     * 图片服务器地址
     */
    ConfigKey<String> IMAGE_SERVER = new ConfigKey<>("image.server", null);
    /**
     * 是否debug模式
     */
    ConfigKey<Boolean> DEBUGMODE = new ConfigKey<>("debugmode", false);
    /**
     * ValidateCodeServlet参数,验证码限定名称
     */
    ConfigKey<String[]> VALIDCODE_NAMES = new ConfigKey<>("names", null);
    /**
     * ValidateCodeServlet参数,验证码图片宽
     */
    ConfigKey<Integer> VALIDCODE_WIDTH = new ConfigKey<>("width", 80);
    /**
     * ValidateCodeServlet参数,验证码图片高
     */
    ConfigKey<Integer> VALIDCODE_HEIGTH = new ConfigKey<>("heigth", 24);
    /**
     * ValidateCodeServlet参数,验证码字符个数
     */
    ConfigKey<Integer> VALIDCODE_COUNT = new ConfigKey<>("count", 4);
    /**
     * IgnoreUrlMatchFilter参数,是否忽略静态文件,默认true
     */
    ConfigKey<Boolean> IGNORE_STATIC = new ConfigKey<>("ignoreStatic", true);
    /**
     * IgnoreUrlMatchFilter参数,要忽略的url正则
     */
    ConfigKey<String> IGNORE_PATTERN = new ConfigKey<>("ignorePattern");
    /**
     * CsrfFilter参数,验证失败中转页面
     */
    ConfigKey<String> CSRF_ERROR_PAGE = new ConfigKey<>("errorPage");
    /**
     * CsrfFilter参数,白名单文件路径
     */
    ConfigKey<String> CSRF_WHITE_FILE = new ConfigKey<>("whiteFile");
    /**
     * LogoutServlet参数,注销后跳转页面
     */
    ConfigKey<String> LOGOUT_PAGE = new ConfigKey<>("logoutPage");
}
