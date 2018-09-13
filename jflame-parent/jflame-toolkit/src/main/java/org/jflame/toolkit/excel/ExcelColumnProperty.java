package org.jflame.toolkit.excel;

import java.beans.PropertyDescriptor;

/**
 * ExcelColumn注解属性封装类
 * 
 * @author yucan.zhang
 */
public class ExcelColumnProperty implements Comparable<ExcelColumnProperty> {

    private PropertyDescriptor propertyDescriptor;
    private String name;
    private int order;
    private int width = 256 * 20;
    private String fmt;
    private String convert;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }

    public String getConvert() {
        return convert;
    }

    public void setConvert(String convert) {
        this.convert = convert;
    }

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public int compareTo(ExcelColumnProperty obj) {
        return this.order - obj.order;
    }
}
