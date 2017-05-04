package org.jflame.mvc.support;

import org.jflame.toolkit.reflect.SpiFactory;
import org.jflame.web.config.ISysConfig;

/**
 * 系统参数获取和定义类
 * @author yucan.zhang
 *
 */
public final class SysParam {
    private static ISysConfig config=SpiFactory.getSingleBean(ISysConfig.class);
    
    /**
     * 参数键名定义接口
     * @author yucan.zhang
     *
     */
    public static interface ParamKey{
        String savePath="save.path";
        String imageSavePath="image.save.path";
        String imageServer="image.server";
    }
    
    /**
     * 文件保存根路径
     * @return
     */
    public static String getSavePath() {
        return config.getTextParam(ParamKey.savePath);
    }
    
    /**
     * 图片保存根路径
     * @return
     */
    public static String getImgSavePath() {
        return config.getTextParam(ParamKey.imageSavePath);
    }
    
    /**
     * 图片服务器地址
     * @return
     */
    public static String getImgServer() {
        return config.getTextParam(ParamKey.imageServer);
    }
}
