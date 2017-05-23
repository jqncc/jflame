package org.jflame.web.util;

import org.jflame.toolkit.reflect.SpiFactory;
import org.jflame.web.config.ISysConfig;
import org.jflame.web.config.BaseSysConfig.DefaultParamKey;

/**
 * 通用静态方法供页面使用
 * @author yucan.zhang
 */
public final class FunctionUtils {

    private static final ISysConfig config = SpiFactory.getSingleBean(ISysConfig.class);

    /**
     * 获取图片保存根路径
     * 
     * @return
     */
    public static String getImgSavePath() {
        return config.getTextParam(DefaultParamKey.imageSavePath);
    }

    /**
     * 获取图片服务器地址
     * 
     * @return
     */
    public static String getImgServer() {
        return config.getTextParam(DefaultParamKey.imageServer);
    }
}
