package cn.huaxunchina.common.jdbcsupport;

import org.springframework.beans.BeanWrapper;

public interface IMetaDataProvider
{

    /**
     * 根据实体类class对象提取对应的表名,列名,主键信息
     * 
     * @param entityClazz 实体类class对象
     * @param transformer 列与属性名称转换器
     * @return
     */
    public abstract TableEntityMetaData extractTableEntityMetaData(Class<?> entityClazz);

    public abstract TableEntityMetaData extractTableEntityMetaData(BeanWrapper beanWrapper);
    
    /**
     * 设置属性名与列名转换实现类
     * @param transformer
     */
    public abstract void setTransformer(IMetaNameTransformer transformer);
}