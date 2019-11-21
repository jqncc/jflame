package org.jflame.db.metadata;

import java.beans.PropertyDescriptor;
import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;

import org.jflame.commons.util.StringHelper;
import org.jflame.db.CamelMetaNameConverter;
import org.jflame.db.IMetaNameConverter;
import org.jflame.db.annotations.Column;
import org.jflame.db.annotations.Id;
import org.jflame.db.annotations.Table;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;

public class DefaultMetaDataProvider implements IMetaDataProvider {

    private static final Map<Class<?>,TableMetaData> metaDataCache = new ConcurrentHashMap<>();

    private IMetaNameConverter columnConvertor;
    private IMetaNameConverter tableConvertor;

    public DefaultMetaDataProvider() {
        this.columnConvertor = new CamelMetaNameConverter();
        this.tableConvertor = new CamelMetaNameConverter();
    }

    @Override
    public TableMetaData extractTableMetaData(Class<?> entityClazz) {
        TableMetaData metaData = metaDataCache.get(entityClazz);
        if (metaData == null) {
            metaData = new TableMetaData();
            // 查找是否有table注解,如果没有注解将实体类名转为下划线分隔方式的表名
            String tableName = null;
            if (entityClazz.isAnnotationPresent(Table.class)) {
                Table table = entityClazz.getAnnotation(Table.class);
                tableName = table.name();
            }
            // 注解未设置表名
            if (StringHelper.isEmpty(tableName)) {
                metaData.setTableName(tableConvertor.propertyToDbname(entityClazz.getSimpleName()));
            }
            metaData.setTableName(tableName);
            PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(entityClazz);
            // 提取列与属性元数据
            setColumMetaData(metaData, pds);
            metaDataCache.put(entityClazz, metaData);
        }
        return metaData;
    }

    @Override
    public TableMetaData extractTableMetaData(BeanWrapper beanWrapper) {
        final Class<?> entityClazz = beanWrapper.getWrappedClass();
        return extractTableMetaData(entityClazz);
    }

    private void setColumMetaData(TableMetaData metaData, PropertyDescriptor[] pds) {
        Method readMethod;
        ColumnMetaData columnMetaData;
        for (PropertyDescriptor pd : pds) {
            readMethod = pd.getReadMethod();
            // 存在Transient注解不处理,非简单类型暂不处理
            if (readMethod.isAnnotationPresent(Transient.class) || readMethod.getReturnType()
                    .equals(Class.class) || !BeanUtils.isSimpleValueType(readMethod.getReturnType())) {
                continue;
            }
            columnMetaData = new ColumnMetaData();
            // @Column
            if (readMethod.isAnnotationPresent(Column.class)) {
                Column c = readMethod.getAnnotation(Column.class);
                columnMetaData.setColumnName(c.name()
                        .toLowerCase());
                columnMetaData.setInsertable(c.insertable());
                columnMetaData.setUpdateable(c.updatable());
            } else {
                columnMetaData.setColumnName(columnConvertor.propertyToDbname(pd.getName()));
            }
            // @id
            if (readMethod.isAnnotationPresent(Id.class)) {
                SetKeyMetaData(metaData, pd, columnMetaData);
            }
            metaData.addProperty(pd.getName(), columnMetaData);
        }
        if (metaData.getKey() == null) {
            // 没有@id注解,尝试查找名为id的属性
            ColumnMetaData idColumn = metaData.getColumnsMap()
                    .get("id");
            if (idColumn != null) {
                PropertyDescriptor idProperty = findPropertyByName(pds, "id");
                SetKeyMetaData(metaData, idProperty, idColumn);
            }
        }
    }

    private void SetKeyMetaData(TableMetaData metaData, PropertyDescriptor idProperty, ColumnMetaData columnMetaData) {
        Method readMethod = idProperty.getReadMethod();
        Id idAnnot = readMethod.getAnnotation(Id.class);
        IdMetaData idMetaData = new IdMetaData(columnMetaData.getColumnName(), idProperty.getName(), idAnnot.idType());
        idMetaData.setPropertyType(idProperty.getPropertyType());
        Annotation[] annots = readMethod.getAnnotations();
        if (annots.length > 1) {
            idMetaData.setPropertyAnnotations(ArrayUtils.removeElement(annots, Id.class));
        }
        metaData.setKey(idMetaData);
        columnMetaData.setPrimaryKey(true);
    }

    private PropertyDescriptor findPropertyByName(PropertyDescriptor[] propertis, String propertyName) {
        for (PropertyDescriptor pd : propertis) {
            if (pd.getName()
                    .equals(propertyName)) {
                return pd;
            }
        }
        return null;
    }

    public void setColumnConvertor(IMetaNameConverter transformer) {
        this.columnConvertor = transformer;
    }

    @Override
    public void setTableConvertor(IMetaNameConverter converter) {
        this.tableConvertor = converter;
    }

}
