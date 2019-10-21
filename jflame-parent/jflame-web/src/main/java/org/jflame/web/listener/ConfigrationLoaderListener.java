package org.jflame.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;

import org.jflame.toolkit.config.PropertiesConfigHolder;
import org.jflame.toolkit.util.StringHelper;

/**
 * servlet容器启动时加载properties配置文件.文件路径由ServletContext参数configFile指定
 * 
 * @author yucan.zhang
 */
public class ConfigrationLoaderListener implements ServletContextListener {

    /**
     * 配置文件路径在ServletContext(web.xml)参数名
     */
    final String CONFIG_FILE_KEY = "configFile";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String configFile = sce.getServletContext()
                .getInitParameter(CONFIG_FILE_KEY);
        if (StringHelper.isNotEmpty(configFile)) {
            String[] propertiesFile = StringHelper.split(StringUtils.deleteWhitespace(configFile));
            PropertiesConfigHolder.loadProperties(propertiesFile);
        } else {
            sce.getServletContext()
                    .log("Not found servletContext parameter 'configFile'");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
