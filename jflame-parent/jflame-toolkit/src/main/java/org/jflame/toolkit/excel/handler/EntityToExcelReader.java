package org.jflame.toolkit.excel.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.ExcelColumnProperty;
import org.jflame.toolkit.excel.ExcelUtils;
import org.jflame.toolkit.excel.IExcelEntity;
import org.jflame.toolkit.exception.ConvertException;

import org.apache.poi.ss.usermodel.Row;

/**
 * excel单行数据与实体bean转换处理器.
 * 
 * @author yucan.zhang
 */
public class EntityToExcelReader<T extends IExcelEntity> implements IExcelRowReader<T> {

    private List<ExcelColumnProperty> columnPropertys = null;
    private Class<T> entityClazz;
    private int propertySize;
    private Object currentValue;
    private ExcelColumnProperty currentProperty;

    /**
     * 构造函数
     * 
     * @param columnPropertys ExcelColumnProperty集合
     * @param dataClass 数据类型
     */
    public EntityToExcelReader(List<ExcelColumnProperty> columnPropertys, Class<T> dataClass) {
        this.columnPropertys = columnPropertys;
        this.entityClazz = dataClass;
        propertySize = columnPropertys.size();
    }

    @Override
    public T extractRow(Row excelSheetRow) {
        T newObj = null;
        try {
            newObj = entityClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ExcelAccessException("failed to new instance for " + entityClazz, e);
        }

        currentValue = null;
        try {
            for (int cellIndex = 0; cellIndex < propertySize; cellIndex++) {
                if (excelSheetRow.getCell(cellIndex) == null) {
                    continue;
                }
                currentProperty = columnPropertys.get(cellIndex);
                currentValue = ExcelUtils.extractValueFromCell(currentProperty,
                        excelSheetRow.getCell(cellIndex));
                if (currentValue != null) {
                    currentProperty.getPropertyDescriptor()
                            .getWriteMethod()
                            .invoke(newObj, currentValue);
                }
            }
        } catch (ConvertException e) {
            String errMsg = String.format("第%d行,'%s'值转换失败", excelSheetRow.getRowNum(), currentProperty.getName());
            throw new ExcelAccessException(errMsg, e);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            String errMsg = String.format("第%d行,'%s'赋值失败", excelSheetRow.getRowNum(), currentProperty.getName());
            throw new ExcelAccessException(errMsg, e);
        }
        return newObj;
    }

}
