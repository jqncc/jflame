package org.jflame.apidoc.model;

import java.io.Serializable;
import java.util.List;

public class ApiElement implements Serializable {

    private static final long serialVersionUID = 618492916484521271L;
    private String elementName;// 名称
    private String elementDataType;// 数据类型
    private String elementDesc;// 说明
    private String defaultValue;// 默认值
    private boolean required = false;// 是否必填
    private List<? extends ApiElement> childElements;// 子元素

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getElementDataType() {
        return elementDataType;
    }

    public void setElementDataType(String elementDataType) {
        this.elementDataType = elementDataType;
    }

    public String getElementDesc() {
        return elementDesc;
    }

    public void setElementDesc(String elementDesc) {
        this.elementDesc = elementDesc;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<? extends ApiElement> getChildElements() {
        return childElements;
    }

    public void setChildElements(List<? extends ApiElement> childElements) {
        this.childElements = childElements;
    }

}
