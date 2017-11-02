package org.jflame.web.tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jflame.toolkit.util.MapHelper;
import org.jflame.toolkit.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * html控件标签接口
 * 
 * @author zyc
 */
public abstract class UIHtmlTag extends BaseTag {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected String name;// 标签name属性
    protected String id;// 标签id属性
    protected String attrs;// 其他html属性，以字符形式组合传入
    protected String style;// 标签css style
    protected String cssClass;// 标签 css class
    protected Integer width;
    protected Integer height;
    protected Map<String,Object> attrMap = new HashMap<>();

    /**
     * 设置标签属性
     * 
     * @param strBuf
     */
    protected void setAttributes(StringBuilder strBuf) {
        if (StringHelper.isNotEmpty(name))
            strBuf.append(" name=\"").append(name).append("\"");
        if (StringHelper.isNotEmpty(id))
            strBuf.append(" id=\"").append(id).append("\"");
        if (StringHelper.isNotEmpty(style)) {
            strBuf.append(" style=\"").append(style).append("\"");
        }
        if (StringHelper.isNotEmpty(cssClass)) {
            strBuf.append(" class=\"").append(cssClass).append("\"");
        }
        if (width != null) {
            strBuf.append(" width=\"").append(width).append("\"");
        }
        if (height != null) {
            strBuf.append(" height=\"").append(height).append("\"");
        }
        if (StringHelper.isNotEmpty(attrs)) {
            strBuf.append(" ").append(attrs);
        }
        if (MapHelper.isNotEmpty(attrMap)) {
            for (Entry<String,Object> kv : attrMap.entrySet()) {
                strBuf.append(" ").append(kv.getKey()).append("=\"").append(kv.getValue()).append("\"");
            }
        }
    }

    public String getAttrs() {
        return attrs;
    }

    public void setAttrs(String attrs) {
        this.attrs = attrs;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

}
