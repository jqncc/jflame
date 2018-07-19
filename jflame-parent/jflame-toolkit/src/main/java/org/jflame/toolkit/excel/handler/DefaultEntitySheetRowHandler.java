package org.jflame.toolkit.excel.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.ExcelColumnProperty;
import org.jflame.toolkit.excel.IExcelEntity;
import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;

/**
 * excel单行数据与实体bean转换处理器.
 * 
 * @author yucan.zhang
 */
public class DefaultEntitySheetRowHandler<T extends IExcelEntity> extends BaseEntitySheetRowHandler<T> {

    // PropertyDescriptor[] properties = null;
    List<ExcelColumnProperty> columnPropertys = null;
    private Class<T> entityClazz;

    /**
     * 构造函数
     * 
     * @param columnPropertys ExcelColumnProperty集合
     * @param dataClass 数据类型
     */
    public DefaultEntitySheetRowHandler(List<ExcelColumnProperty> columnPropertys, Class<T> dataClass) {
        // this.properties = properties;
        this.columnPropertys = columnPropertys;
        this.entityClazz = dataClass;
    }

    @Override
    public void fillRow(T rowData, Row excelSheetRow) {
        Cell cell = null;
        int cellIndex = 0;
        int size = columnPropertys.size();
        for (cellIndex = 0; cellIndex < size; cellIndex++) {
            cell = excelSheetRow.createCell(cellIndex);
            setPropertyToCell(rowData, columnPropertys.get(cellIndex), cell);
        }
    }

    /* @Override
    public PropertyDescriptor[] getProperties() {
        return properties;
    }*/

    @Override
    public List<ExcelColumnProperty> getColumnPropertys() {
        return columnPropertys;
    }

    @Override
    public T extractRow(Row excelSheetRow) {
        T newObj = null;
        try {
            newObj = entityClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ExcelAccessException("failed to new instance for " + entityClazz, e);
        }
        List<ExcelColumnProperty> lstDescriptors = getColumnPropertys();
        int size = lstDescriptors.size();
        ExcelColumnProperty cProperty;
        Object newValue = null;
        for (int i = 0; i < size; i++) {
            if (excelSheetRow.getCell(i) == null) {
                continue;
            }
            cProperty = lstDescriptors.get(i);
            newValue = ExcelConvertorSupport.convertValueFromCellValue(cProperty, excelSheetRow.getCell(i));
            if (newValue != null) {
                try {
                    cProperty.getPropertyDescriptor().getWriteMethod().invoke(newObj, newValue);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new ExcelAccessException("赋值失败,第" + excelSheetRow.getRowNum() + "行 " + cProperty.getName(),
                            e);
                }
            }
        }
        return newObj;
    }

    private void setPropertyToCell(T object, ExcelColumnProperty cproperty, Cell cell) {
        Method methodGetX;
        Object propertyValue;
        try {
            methodGetX = cproperty.getPropertyDescriptor().getReadMethod();
            propertyValue = methodGetX.invoke(object);
            if (propertyValue != null && !"".equals(propertyValue)) {
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(ExcelConvertorSupport.convertToCellValue(cproperty, propertyValue));
            }
            /* for (PropertyDescriptor pd : properties) {
                if (pd.getName().equals(cproperty.propertyName)) {
                    methodGetX = pd.getReadMethod();
                    propertyValue = methodGetX.invoke(object);
                    if (propertyValue != null && !"".equals(propertyValue)) {
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(ExcelConvertorSupport.convertToCellValue(cproperty.convert, propertyValue,
                                cproperty.fmt));
                    }
                }
            }*/
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ExcelAccessException(e);
        }
    }

}
