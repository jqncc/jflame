package org.jflame.commons.excel;

import java.beans.PropertyDescriptor;

import org.jflame.commons.convert.Converter;
import org.jflame.commons.convert.ObjectToStringConverter;

/**
 * ExcelColumn注解属性封装类
 * 
 * @author yucan.zhang
 */
@SuppressWarnings("rawtypes")
public class ExcelColumnProperty implements Comparable<ExcelColumnProperty> {

    private PropertyDescriptor propertyDescriptor;
    private String name;
    private int order;
    private int width = 256 * 20;
    private String fmt;
    private ObjectToStringConverter writeConverter;
    private Converter readConverter;

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

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public ObjectToStringConverter getWriteConverter() {
        return writeConverter;
    }

    public void setWriteConverter(ObjectToStringConverter writeConverter) {
        this.writeConverter = writeConverter;
    }

    public Converter getReadConverter() {
        return readConverter;
    }

    public void setReadConverter(Converter readConverter) {
        this.readConverter = readConverter;
    }

    public int compareTo(ExcelColumnProperty obj) {
        return this.order - obj.order;
    }
}
