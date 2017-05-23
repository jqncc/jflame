package org.jflame.mvc.support;

import org.jflame.toolkit.reflect.SpiFactory;
import org.jflame.web.config.BaseSysConfig.DefaultParamKey;
import org.jflame.web.config.ISysConfig;

public final class SysParam {

    private static ISysConfig config = SpiFactory.getSingleBean(ISysConfig.class);

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
        return config.getTextParam(DefaultParamKey.savePath);
    }

    /**
     * 图片保存根路径
     * 
     * @return
     */
    public static String getImgSavePath() {
        return config.getTextParam(DefaultParamKey.imageSavePath);
    }

    /**
     * 图片服务器地址
     * 
     * @return
     */
    public static String getImgServer() {
        return config.getTextParam(DefaultParamKey.imageServer);
    }
    
}
