package org.jflame.db;

import org.jflame.commons.util.StringHelper;

/**
 * 数据列元数据转实体类时名称实现类
 * <p>
 * 列名规则:多个单词用下划线隔开.属性名使用驼峰命名
 * 
 * @author zyc
 */
public class CamelMetaNameConverter implements IMetaNameConverter {

    private boolean upperCase = false;// 是否大写

    public CamelMetaNameConverter() {
    }

    public CamelMetaNameConverter(boolean upperCase) {
        this.upperCase = upperCase;
    }

    public void setUpperCase(boolean upperCase) {
        this.upperCase = upperCase;
    }

    @Override
    public String dbnameToProperty(String columnName) {
        if (StringHelper.isEmpty(columnName)) {
            throw new IllegalArgumentException("column name can't be blank");
        }
        return StringHelper.underlineToCamel(columnName.toLowerCase());
    }

    @Override
    public String propertyToDbname(String propertyName) {
        if (StringHelper.isEmpty(propertyName)) {
            throw new IllegalArgumentException("propertyName name can't be blank");
        }
        return upperCase?StringHelper.camelToUnderline(propertyName).toUpperCase():StringHelper.camelToUnderline(propertyName);
    }

}
