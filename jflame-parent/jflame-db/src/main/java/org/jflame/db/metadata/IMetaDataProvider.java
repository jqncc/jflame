package org.jflame.db.metadata;

import org.jflame.db.IMetaNameConverter;
import org.springframework.beans.BeanWrapper;

public interface IMetaDataProvider {

    /**
     * 根据实体类class对象提取对应的表名,列名,主键信息
     * 
     * @param entityClazz 实体类class对象
     * @param transformer 列与属性名称转换器
     * @return
     */
    public abstract TableMetaData extractTableMetaData(Class<?> entityClazz);

    public abstract TableMetaData extractTableMetaData(BeanWrapper beanWrapper);

    /**
     * 设置属性名与列名转换类
     * 
     * @param converter
     */
    public abstract void setColumnConvertor(IMetaNameConverter converter);

    /**
     * 设置表名与属性名转换类
     * 
     * @param converter
     */
    public abstract void setTableConvertor(IMetaNameConverter converter);
}