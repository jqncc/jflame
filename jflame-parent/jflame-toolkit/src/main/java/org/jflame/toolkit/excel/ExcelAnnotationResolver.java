package org.jflame.toolkit.excel;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ExcelColumn注解解析类
 * 
 * @see ExcelColumn
 * @author yucan.zhang
 */
public class ExcelAnnotationResolver {

    /**
     * 根据excel column获取bean的属性.
     * 
     * @param properties bean属性描述对象
     * @return excel column注解属性
     */
    public List<ExcelColumnProperty> getColumnPropertysByAnnons(PropertyDescriptor[] properties) {
        List<ExcelColumnProperty> as = new ArrayList<ExcelColumnProperty>();
        Method methodGetX;
        final Class<ExcelColumn> clazz = ExcelColumn.class;
        ExcelColumnProperty newProperty;
        ExcelColumn tmpAnns;
        for (PropertyDescriptor propertyDescriptor : properties) {
            methodGetX = propertyDescriptor.getReadMethod();
            if (methodGetX.isAnnotationPresent(clazz)) {
                tmpAnns = methodGetX.getAnnotation(clazz);
                newProperty = new ExcelColumnProperty();
                newProperty.setPropertyDescriptor(propertyDescriptor);
                newProperty.setOrder(tmpAnns.order());
                newProperty.setConvert(tmpAnns.convert());
                newProperty.setFmt(tmpAnns.fmt());
                newProperty.setName(tmpAnns.name());
                newProperty.setWidth(tmpAnns.width());
                as.add(newProperty);
            }
        }
        Collections.sort(as);
        return as;
    }

    /**
     * 获取指定属性名的属性.排序按照propertyNames的顺序
     * 
     * @param properties bean属性描述对象
     * @param propertyNames 指定的属性名数据组
     * @return excel column注解属性
     */
    public List<ExcelColumnProperty> getColumnPropertysByName(PropertyDescriptor[] properties, String[] propertyNames) {
        List<ExcelColumnProperty> as = new ArrayList<ExcelColumnProperty>();
        Method methodGetX;
        final Class<ExcelColumn> clazz = ExcelColumn.class;
        ExcelColumnProperty newProperty;
        ExcelColumn tmpAnns;
        int i = 0;
        for (String property : propertyNames) {
            for (PropertyDescriptor pd : properties) {
                if (pd.getName().equals(property)) {
                    methodGetX = pd.getReadMethod();
                    if (methodGetX != null) {
                        newProperty = new ExcelColumnProperty();
                        newProperty.setPropertyDescriptor(pd);
                        if (methodGetX.isAnnotationPresent(clazz)) {
                            tmpAnns = methodGetX.getAnnotation(clazz);
                            newProperty.setConvert(tmpAnns.convert());
                            newProperty.setFmt(tmpAnns.fmt());
                            newProperty.setName(tmpAnns.name());
                            newProperty.setWidth(tmpAnns.width());
                        }
                        newProperty.setOrder(i++);
                        as.add(newProperty);
                    }
                }
            }
        }
        Collections.sort(as);
        return as;
    }
}
