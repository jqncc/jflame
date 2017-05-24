package org.jflame.web.tag;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;

import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.util.FunctionUtils;
import org.jflame.web.util.WebUtils;

/**
 * 图片全路径显示标签
 * <p>
 * 示例:&ltjf:img src="/2017/4/gg.jpg" /&gt; ==> &ltimg src="/app/loadImg/2017/4/gg.jpg" /&gt;
 * 
 * @author yucan.zhang
 */
public class UIImageUrlTag extends UIHtmlTag{

    private final String attrName = "baseImgUrl";
    private String src;

    @Override
    public void doTag() throws JspException, IOException {
        StringBuilder strBuf = new StringBuilder(50);
        strBuf.append("<img src=\"");
        if (StringHelper.isNotEmpty(src)) {
            /* 绝对路径图片直接输出,相对路径合并图片服务器根地址 */
            if (WebUtils.isAbsoluteUrl(src)) {
                strBuf.append(src);
            } else {
                strBuf.append(WebUtils.mergeUrl(getBaseImgUrl(), src));
            }
        }
        strBuf.append("\"");
        setAttributes(strBuf);
        strBuf.append(">");
        getOut().print(strBuf);
    }

    /**
     * 取图片根路径
     * 
     * @return
     */
    private String getBaseImgUrl() {
        ServletContext application = getServletContext();
        String baseImgUrl = (String) application.getAttribute(attrName);
        if (baseImgUrl == null) {
            synchronized (this) {
                String imgServer = FunctionUtils.getImgServer();
                if (StringHelper.isNotEmpty(imgServer) && WebUtils.isAbsoluteUrl(imgServer)) {
                    baseImgUrl = imgServer;
                } else {
                    baseImgUrl = WebUtils.mergeUrl(getContextPath(), imgServer);
                }
            }
            application.setAttribute(attrName, baseImgUrl);
        }
        return baseImgUrl;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setAlt(String alt) {
        attrMap.put("alt", alt);
    }

    @Override
    protected void setDataSource() {
    }
}
