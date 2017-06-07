package org.jflame.db.metadata;

import java.lang.annotation.Annotation;

import org.jflame.db.id.IdType;

/**
 * 主键元数据,单列主键
 * @author yucan.zhang
 *
 */
public class IdMetaData {

    private String columnName;//主键列名
    private String propertyName;//对应属性名
    private Class<?> propertyType;//对应属性类型
    private IdType idType;//主键生成方式
    private Annotation[] propertyAnnotations;//属性上标注的注解

    public IdMetaData() {
    }

    public IdMetaData(String columnName, String propertyName, IdType idType) {
        super();
        this.columnName = columnName;
        this.propertyName = propertyName;
        this.idType = idType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(Class<?> propertyType) {
        this.propertyType = propertyType;
    }

    public Annotation[] getPropertyAnnotations() {
        return propertyAnnotations;
    }

    public void setPropertyAnnotations(Annotation[] propertyAnnotations) {
        this.propertyAnnotations = propertyAnnotations;
    }

}
