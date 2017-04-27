package org.jflame.web;

import java.util.Map;

/**
 * 系统配置参数接口
 * 
 * @author yucan.zhang
 */
public interface ISysConfig {

    /**
     * 加载所有配置参数
     */
    void loadConfig();

    /**
     * 重载所有配置参数
     */
    void reloadConfig();

    /**
     * 取得所有配置参数
     * 
     * @return
     */
    Map<String,Object> getAllParams();

    /**
     * 根据参数名取得参数值
     * 
     * @param paramKey 配置参数名
     * @return
     */
    Object getParam(String paramKey);
    /**
     * 根据参数名取得参数字符串值
     * @param paramKey 配置参数名
     * @return
     */
    String getTextParam(String paramKey);
}
