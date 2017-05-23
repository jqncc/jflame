package cn.huaxunchina.common.jdbcsupport;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;

public class TEMetaDataProvider implements IMetaDataProvider
{
    private static final Map<Class<?>, TableEntityMetaData> metaDataCache = new LinkedHashMap<>();
    private IMetaNameTransformer transformer;

    public TEMetaDataProvider()
    {
        this.transformer = new CamelMetaNameTransformer();
    }

    public TEMetaDataProvider(IMetaNameTransformer transformer)
    {
        this.transformer = transformer;
    }

    @Override
    public TableEntityMetaData extractTableEntityMetaData(Class<?> entityClazz)
    {
        synchronized (metaDataCache)
        {
            if (metaDataCache.containsKey(entityClazz))
            {
                return metaDataCache.get(entityClazz);
            }
        }
        TableEntityMetaData metaData = new TableEntityMetaData();
        // 查找是否有table注解,如果没有注解将实体类名转为下划线分隔方式的表名
        if (entityClazz.isAnnotationPresent(Table.class))
        {
            Table table = entityClazz.getAnnotation(Table.class);
            metaData.setTableName(table.name());
        } else
        {
            metaData.setTableName(transformer.propertyToColumn(entityClazz.getSimpleName()));
        }
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(entityClazz);
        setColumMetaData(transformer, metaData, pds);
        metaDataCache.put(entityClazz, metaData);
        return metaData;
    }

    @Override
    public TableEntityMetaData extractTableEntityMetaData(BeanWrapper beanWrapper)
    {
        final Class<?> entityClazz = beanWrapper.getWrappedClass();
        return extractTableEntityMetaData(entityClazz);
    }

    private static void setColumMetaData(IMetaNameTransformer transformer, TableEntityMetaData metaData, PropertyDescriptor[] pds)
    {
        Method readMethod;
        ColumnMetaData cmd;
        for (PropertyDescriptor pd : pds)
        {
            readMethod = pd.getReadMethod();
            // 存在Transient注解不处理,非简单类型暂不处理
            if (readMethod.isAnnotationPresent(Transient.class) || readMethod.getReturnType().equals(Class.class)
                    || !BeanUtils.isSimpleValueType(readMethod.getReturnType()))
            {
                continue;
            }
            cmd = new ColumnMetaData();
            // @Column
            if (readMethod.isAnnotationPresent(Column.class))
            {
                Column c = readMethod.getAnnotation(Column.class);
                cmd.setColumnName(c.name().toUpperCase());
                cmd.setInsertable(c.insertable());
                cmd.setUpdateable(c.updatable());
            } else
            {
                cmd.setColumnName(transformer.propertyToColumn(pd.getName()));
            }
            // @id
            if (readMethod.isAnnotationPresent(Id.class))
            {
                metaData.setKeyProperty(pd.getName());
                metaData.setKeyColumn(cmd.getColumnName());
                cmd.setPrimaryKey(true);
                metaData.setKeyGenerated(readMethod.isAnnotationPresent(GeneratedValue.class));
            }
            metaData.addProperty(pd.getName(), cmd);
        }
    }

    public IMetaNameTransformer getTransformer()
    {
        return transformer;
    }

    public void setTransformer(IMetaNameTransformer transformer)
    {
        this.transformer = transformer;
    }

}
