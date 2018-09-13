package org.jflame.toolkit.excel;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jflame.toolkit.reflect.BeanHelper;

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
     * @param dataClass Class&lt;? extends IExcelEntity&gt;
     * @return excel column注解属性
     */
    public List<ExcelColumnProperty> getColumnPropertysByAnnons(Class<? extends IExcelEntity> dataClass) {
        List<ExcelColumnProperty> as = new ArrayList<ExcelColumnProperty>();

        final Class<ExcelColumn> clazz = ExcelColumn.class;
        final String clazzName = "class";

        ExcelColumnProperty newProperty;
        ExcelColumn tmpAnns;
        Field tmpField;
        Method tmpReadMethod;
        PropertyDescriptor[] properties = BeanHelper.getPropertyDescriptors(dataClass);
        for (PropertyDescriptor propDesc : properties) {
            if (clazzName.equals(propDesc.getName())) {
                continue;
            }
            tmpAnns = null;
            tmpReadMethod = propDesc.getReadMethod();
            if (tmpReadMethod.isAnnotationPresent(clazz)) {
                tmpAnns = tmpReadMethod.getAnnotation(clazz);
            }
            if (tmpAnns == null) {
                tmpField = FieldUtils.getField(dataClass, propDesc.getName(), true);
                if (tmpField != null && tmpField.isAnnotationPresent(clazz)) {
                    tmpAnns = tmpField.getAnnotation(clazz);
                }
            }
            if (tmpAnns != null) {
                newProperty = new ExcelColumnProperty();
                newProperty.setPropertyDescriptor(propDesc);
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
     * @paramdataClass Class&lt;? extends IExcelEntity&gt;
     * @param propertyNames 指定的属性名数据组
     * @return excel column注解属性
     */
    public List<ExcelColumnProperty> getColumnPropertysByName(Class<? extends IExcelEntity> dataClass,
            String[] propertyNames) {
        List<ExcelColumnProperty> as = new ArrayList<ExcelColumnProperty>();
        final Class<ExcelColumn> clazz = ExcelColumn.class;
        ExcelColumnProperty newProperty;
        ExcelColumn tmpAnns;
        int i = 0;
        Field tmpField;
        Method tmpReadMethod;
        PropertyDescriptor[] properties = BeanHelper.getPropertyDescriptors(dataClass);
        for (String property : propertyNames) {
            for (PropertyDescriptor pd : properties) {
                if (pd.getName().equals(property)) {
                    tmpAnns = null;
                    tmpReadMethod = pd.getReadMethod();
                    if (tmpReadMethod.isAnnotationPresent(clazz)) {
                        tmpAnns = tmpReadMethod.getAnnotation(clazz);
                    }
                    if (tmpAnns == null) {
                        tmpField = FieldUtils.getField(dataClass, pd.getName(), true);
                        if (tmpField != null && tmpField.isAnnotationPresent(clazz)) {
                            tmpAnns = tmpField.getAnnotation(clazz);
                        }
                    }
                    if (tmpAnns != null) {
                        newProperty = new ExcelColumnProperty();
                        newProperty.setPropertyDescriptor(pd);
                        newProperty.setOrder(tmpAnns.order());
                        newProperty.setConvert(tmpAnns.convert());
                        newProperty.setFmt(tmpAnns.fmt());
                        newProperty.setName(tmpAnns.name());
                        newProperty.setWidth(tmpAnns.width());
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
