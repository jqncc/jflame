package org.jflame.mvc.support;

import org.jflame.web.config.DefaultConfigKeys;
import org.jflame.web.config.PropertiesConfig;

public final class SysParam {

    //private static ISysConfig config = SpiFactory.getSingleBean(ISysConfig.class);
    
    private static PropertiesConfig config;
    static {
        config=new PropertiesConfig("system.properties");
    }

    /**
     * 参数键名定义接口
     * 
     * @author yucan.zhang
     */
    public interface ParamKey {
      
    }

    /**
     * 文件保存根路径
     * 
     * @return
     */
    public static String getSavePath() {
        return config.getString(DefaultConfigKeys.SAVE_PATH);
    }

    /**
     * 图片保存根路径
     * 
     * @return
     */
    public static String getImgSavePath() {
        return config.getString(DefaultConfigKeys.IMAGE_SAVE_PATH);
    }

    /**
     * 图片服务器地址
     * 
     * @return
     */
    public static String getImgServer() {
        return config.getString(DefaultConfigKeys.IMAGE_SERVER);
    }
    
}
