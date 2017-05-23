package org.jflame.web.tag;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.jflame.toolkit.reflect.SpiFactory;
import org.jflame.web.config.ISysConfig;


@SuppressWarnings("serial")
public abstract class BaseTag extends TagSupport {
    protected ISysConfig sysConfig = SpiFactory.getSingleBean(ISysConfig.class);
    
    protected HttpServletRequest getRequest(){
        return (HttpServletRequest)pageContext.getRequest();
    }
    
    protected ServletContext getServletContext(){
        return pageContext.getServletContext();
    }
    
    protected String getContextPath(){
        return getRequest().getContextPath();
    }
}
