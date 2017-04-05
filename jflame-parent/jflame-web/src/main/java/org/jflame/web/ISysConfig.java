package org.jflame.web;

import java.util.Map;

public interface ISysConfig {

    void loadConfig();
    
    void reloadConfig();
    
    Map<String,Object> getAllConfigParam();

    Object getConfigParam(String paramKey);
}
