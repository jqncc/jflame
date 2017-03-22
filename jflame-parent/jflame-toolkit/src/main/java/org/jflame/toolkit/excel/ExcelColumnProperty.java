package org.jflame.toolkit.excel;

/**
 * ExcelColumn注解属性封装类
 * @author yucan.zhang
 */
public class ExcelColumnProperty implements Comparable<ExcelColumnProperty> {
    public String propertyName;
    public String name;
    public int order;
    public int width = 256 * 20;
    public String fmt;
    public String convert;

    public int compareTo(ExcelColumnProperty obj) {
        return this.order - obj.order;
    }
}
