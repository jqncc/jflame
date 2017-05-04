package org.jflame.mvc.support;

import org.jflame.toolkit.exception.BusinessException;
import org.jflame.web.config.BaseSysConfig;

/**
 * 系统配置参数加载类
 * @author yucan.zhang
 */
public class SysConfigLoader extends BaseSysConfig {

    public SysConfigLoader() throws BusinessException {
        super("system.properties");
    }


    @Override
    protected void loadFromDb() {
    }
}
