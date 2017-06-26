package org.jflame.web.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.jflame.toolkit.util.StringHelper;

/**
 * 图片验证码生成标签. 示例:
 * 
 * <pre>
 * &lt;jf:validcode width="100" height="40" codeCount="4" codeName="regcode" id="validcode" /&gt;
 * &lt;jf:validcode codeName="logincode" /&gt;
 * &lt;jf:validcode /&gt;
 * </pre>
 * 
 * @author yucan.zhang
 */
public class ValidateCodeTag extends UIHtmlTag {

    private Integer width;
    private Integer height;
    private Integer codeCount;
    private String codeName;
    private final String tagFmt = "<img src=\"%2$s/validcode?rn=1%1$s\" title=\"点击刷新\" onclick=\"this.src='%2$s/validcode?rn=1%1$s&r='+Math.random()\"";

    @Override
    public void doTag() throws JspException, IOException {
        String paramStr = "";
        if (width != null) {
            paramStr = paramStr + "&w=" + width;
        }
        if (height != null) {
            paramStr = paramStr + "&h=" + height;
        }
        if (codeCount != null) {
            paramStr = paramStr + "&c=" + codeCount;
        }
        if (StringHelper.isNotEmpty(codeName)) {
            paramStr = paramStr + "&n=" + codeName;
        }
        StringBuilder strBuilder = new StringBuilder(100);
        strBuilder.append(String.format(tagFmt, paramStr, getContextPath()));
        setAttributes(strBuilder);
        strBuilder.append(" >");

        getOut().print(strBuilder);

    }


    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setCodeCount(Integer codeCount) {
        this.codeCount = codeCount;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

}
