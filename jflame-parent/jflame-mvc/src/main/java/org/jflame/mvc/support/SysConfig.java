package org.jflame.mvc.support;

import java.util.Map;

import org.jflame.web.ISysConfig;


public class SysConfig implements ISysConfig {

    @Override
    public void loadConfig() {
    }

    @Override
    public void reloadConfig() {
    }

    @Override
    public Map<String,Object> getAllParams() {
        return null;
    }

    @Override
    public Object getParam(String paramKey) {
        return null;
    }

    @Override
    public String getTextParam(String paramKey) {
        return null;
    }

}
