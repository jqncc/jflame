package org.jflame.toolkit.excel.handler;

import java.beans.PropertyDescriptor;
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
public class DefaultEntitySheetRowHandler extends BaseEntitySheetRowHandler<IExcelEntity> {

    PropertyDescriptor[] properties = null;
    List<ExcelColumnProperty> columnPropertys = null;

    public DefaultEntitySheetRowHandler(PropertyDescriptor[] properties, List<ExcelColumnProperty> columnPropertys) {
        this.properties = properties;
        this.columnPropertys = columnPropertys;
    }

    @Override
    public void fillRow(IExcelEntity rowData, Row excelSheetRow) {
        Cell cell = null;
        int cellIndex = 0;
        int size = columnPropertys.size();
        for (cellIndex = 0; cellIndex < size; cellIndex++) {
            cell = excelSheetRow.createCell(cellIndex);
            setPropertyToCell(properties, rowData, columnPropertys.get(cellIndex), cell);
        }
    }

    @Override
    public IExcelEntity extractRow(Row excelSheetRow) {
        return null;
    }

    private void setPropertyToCell(PropertyDescriptor[] properties, IExcelEntity object, ExcelColumnProperty cproperty,
            Cell cell) {
        Method methodGetX;
        Object propertyValue;
        try {
            for (PropertyDescriptor pd : properties) {
                if (pd.getName().equals(cproperty.propertyName)) {
                    methodGetX = pd.getReadMethod();
                    propertyValue = methodGetX.invoke(object);
                    if (propertyValue != null) {
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(ExcelConvertorSupport.convertToCellValue(cproperty.convert, propertyValue,
                                cproperty.fmt));
                    }
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ExcelAccessException(e);
        }
    }

    @Override
    public PropertyDescriptor[] getProperties() {
        return properties;
    }

    @Override
    public List<ExcelColumnProperty> getColumnPropertys() {
        return columnPropertys;
    }

}
