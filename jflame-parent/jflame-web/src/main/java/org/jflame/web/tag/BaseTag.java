package org.jflame.web.tag;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.jflame.toolkit.reflect.SpiFactory;
import org.jflame.web.config.ISysConfig;


public abstract class BaseTag extends SimpleTagSupport  {
    protected ISysConfig sysConfig = SpiFactory.getSingleBean(ISysConfig.class);
    
    protected HttpServletRequest getRequest(){
        PageContext pageContext=(PageContext)this.getJspContext();
        return (HttpServletRequest)pageContext.getRequest();
    }
    
    protected ServletContext getServletContext(){
        PageContext pageContext=(PageContext)this.getJspContext();
        return pageContext.getServletContext();
    }
    
    protected String getContextPath(){
        return getRequest().getContextPath();
    }
    
    protected JspWriter getOut(){
        return this.getJspContext().getOut();
    }
}
