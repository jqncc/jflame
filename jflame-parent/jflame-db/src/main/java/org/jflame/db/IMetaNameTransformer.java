package cn.huaxunchina.common.jdbcsupport;
/**
 * 数据表元数据转实体类时名称的转换接口
 * @author zyc
 */
public interface IMetaNameTransformer
{
    /**
     * 数据表列名转实体类属性名
     * 
     * @param columnName
     * @return
     */
    public String columnToProperty(String columnName);

    /**
     * 实体类属性名转数据表列名
     * 
     * @param propertyName
     * @return
     */
    public String propertyToColumn(String propertyName);
}
