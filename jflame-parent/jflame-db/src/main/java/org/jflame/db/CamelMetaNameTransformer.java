package cn.huaxunchina.common.jdbcsupport;

import cn.huaxunchina.toolkit.util.StringUtil;

/**
 * 数据列元数据转实体类时名称实现类
 * <p>
 * 列名规则:大写,多个单词用下划线隔开.属性名使用驼峰命名
 * 
 * @author zyc
 */
public class CamelMetaNameTransformer implements IMetaNameTransformer
{
    @Override
    public String columnToProperty(String columnName)
    {
        if (columnName == null || columnName.isEmpty())
        {
            throw new IllegalArgumentException("column name can't be blank");
        }
        columnName = columnName.toLowerCase();
        return StringUtil.underlineToCamel(columnName);
    }

    @Override
    public String propertyToColumn(String propertyName)
    {
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new IllegalArgumentException("propertyName name can't be blank");
        }
        return StringUtil.camelToUnderline(propertyName).toUpperCase();
    }

}
