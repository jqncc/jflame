package org.jflame.toolkit.excel.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.ExcelColumnProperty;
import org.jflame.toolkit.excel.IExcelEntity;
import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;
import org.jflame.toolkit.exception.ConvertException;

/**
 * excel单行数据与实体bean转换处理器.
 * 
 * @author yucan.zhang
 */
public class DefaultEntitySheetRowHandler<T extends IExcelEntity> extends BaseEntitySheetRowHandler<T> {

    private List<ExcelColumnProperty> columnPropertys = null;
    private Class<T> entityClazz;
    private int propertySize;

    /**
     * 构造函数
     * 
     * @param columnPropertys ExcelColumnProperty集合
     * @param dataClass 数据类型
     */
    public DefaultEntitySheetRowHandler(List<ExcelColumnProperty> columnPropertys, Class<T> dataClass) {
        this.columnPropertys = columnPropertys;
        this.entityClazz = dataClass;
        propertySize = columnPropertys.size();
    }

    private Cell currentCell = null;
    private Object currentValue;
    private ExcelColumnProperty currentProperty;
    private int cellIndex = 0;

    @Override
    public void fillRow(T rowData, Row excelSheetRow) {
        cellIndex = 0;
        for (cellIndex = 0; cellIndex < propertySize; cellIndex++) {
            currentCell = excelSheetRow.createCell(cellIndex);
            currentProperty = columnPropertys.get(cellIndex);
            try {
                currentValue = currentProperty.getPropertyDescriptor().getReadMethod().invoke(rowData);
                if (currentValue != null && !StringUtils.EMPTY.equals(currentValue)) {
                    currentCell.setCellType(CellType.STRING);
                    currentCell.setCellValue(ExcelConvertorSupport.convertToCellValue(currentProperty, currentValue));
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new ExcelAccessException(e);
            }
        }
    }

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
        // List<ExcelColumnProperty> lstDescriptors = getColumnPropertys();
        // int size = columnPropertys.size();
        // ExcelColumnProperty cProperty;
        currentValue = null;
        for (cellIndex = 0; cellIndex < propertySize; cellIndex++) {
            if (excelSheetRow.getCell(cellIndex) == null) {
                continue;
            }
            currentProperty = columnPropertys.get(cellIndex);
            try {
                currentValue = ExcelConvertorSupport.extractFromCellValue(currentProperty,
                        excelSheetRow.getCell(cellIndex));
                if (currentValue != null) {
                    currentProperty.getPropertyDescriptor().getWriteMethod().invoke(newObj, currentValue);
                }
            } catch (ConvertException e) {
                String errMsg = String.format("第%d行,'%s'值转换失败", excelSheetRow.getRowNum(), currentProperty.getName());
                throw new ExcelAccessException(errMsg, e);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                String errMsg = String.format("第%d行,'%s'赋值失败", excelSheetRow.getRowNum(), currentProperty.getName());
                throw new ExcelAccessException(errMsg, e);
            }
        }
        return newObj;
    }

}
