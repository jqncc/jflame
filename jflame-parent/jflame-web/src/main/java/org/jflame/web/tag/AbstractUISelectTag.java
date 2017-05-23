package org.jflame.web.tag;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.jflame.toolkit.common.bean.pair.IKeyValuePair;
import org.jflame.toolkit.common.bean.pair.KeyValuePair;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * select标签抽象类
 * 
 * @author zyc
 */
@SuppressWarnings("serial")
public abstract class AbstractUISelectTag extends UIHtmlTag {

    private String value;// 传入值,即选择值
    private String defaultValue;// 默认选项值
    private String defaultText;// 默认选项文本

    public int doStartTag() throws JspException {
        StringBuilder strBuf = new StringBuilder(50);
        strBuf.append("<select");
        setAttributes(strBuf);
        strBuf.append(">");
        // 添加默认选项
        if (StringHelper.isNotEmpty(defaultText) || StringHelper.isNotEmpty(defaultValue)) {
            if (defaultText == null)
                defaultText = "-请选择-";
            if (defaultValue == null)
                defaultValue = "";
            strBuf.append("<option value=\"").append(defaultValue);
            strBuf.append("\">").append(defaultText).append("</option>");
        }

        List<? extends KeyValuePair<?,?>> dataSource = getBindData();
        if (!CollectionHelper.isEmpty(dataSource)) {
            for (IKeyValuePair<?,?> nvPair : dataSource) {
                strBuf.append("<option value=\"").append(nvPair.getKey());
                strBuf.append("\"");
                if (value != null && value.equals(nvPair.getKey()))
                    strBuf.append(" selected=\"selected\"");
                strBuf.append(">").append(nvPair.getValue()).append("</option>");
            }
        }
        strBuf.append("</select>");
        try {
            pageContext.getOut().print(strBuf);
        } catch (IOException e) {
            logger.error("ui:selectEnum error", e);
        }
        return TagSupport.SKIP_BODY;
    }

    @Override
    protected void setDataSource() {
    };

    /**
     * 获取绑定的数据源
     * 
     * @return
     */
    protected abstract List<? extends KeyValuePair<?,?>> getBindData();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

}
