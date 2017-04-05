package org.jflame.web.tag;

import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * html控件标签接口
 * 
 * @author zyc
 */
@SuppressWarnings("serial")
public abstract class UIHtmlTag extends TagSupport
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected String name;//标签name属性
    protected String id;//标签id属性
    protected String attrs;//其他html属性，以字符形式组合传入
    protected String cssStyle;//标签css style
    protected String cssClass;//标签 css class
    /**
     * 数据源,具体类型由子类定义或实现:<br>
     * 
     */
    protected Object data;
    
    /**
     * 设置标签属性
     * @param strBuf
     */
    protected void setAttributes(StringBuilder strBuf){
        if (name != null && "".equals(name))
            strBuf.append(" name=\"").append(name).append("\"");
        if (id != null && "".equals(id))
            strBuf.append(" id=\"").append(id).append("\"");
        if(cssStyle!=null){
            strBuf.append(" style=\"").append(cssStyle).append("\"");
        }
        if(cssClass!=null){
            strBuf.append(" cssClass=\"").append(cssClass).append("\"");
        }
        if (attrs != null && "".equals(attrs))
        {
            strBuf.append(" ").append(attrs);
        } 
    }
    
    public String getAttrs()
    {
        return attrs;
    }

    public void setAttrs(String attrs)
    {
        this.attrs = attrs;
    }

    public String getCssStyle()
    {
        return cssStyle;
    }

    public void setCssStyle(String cssStyle)
    {
        this.cssStyle = cssStyle;
    }

    public String getCssClass()
    {
        return cssClass;
    }

    public void setCssClass(String cssClass)
    {
        this.cssClass = cssClass;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

}
