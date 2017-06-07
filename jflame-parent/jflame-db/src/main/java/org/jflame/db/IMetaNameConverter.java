package org.jflame.db;

/**
 * 数据表元数据转实体类时名称的转换接口
 * 
 * @author zyc
 */
public interface IMetaNameConverter {

    /**
     * 数据库元素(表,列,视图名)名转类名
     * 
     * @param dbname
     * @return
     */
    public String dbnameToProperty(String dbname);

    /**
     * 实体类属性名转数据库元素名(表,列,视图名)
     * 
     * @param propertyName
     * @return
     */
    public String propertyToDbname(String propertyName);
}
