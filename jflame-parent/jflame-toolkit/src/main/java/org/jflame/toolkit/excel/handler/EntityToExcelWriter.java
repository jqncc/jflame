package org.jflame.toolkit.excel.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.ExcelColumnProperty;
import org.jflame.toolkit.excel.ExcelConvertorSupport;
import org.jflame.toolkit.excel.IExcelEntity;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 * excel单行数据与实体bean转换处理器.
 * 
 * @author yucan.zhang
 */
public class EntityToExcelWriter<T extends IExcelEntity> implements IExcelRowWriter<T> {

    private int propertySize;
    private Cell currentCell = null;
    private Object currentValue;
    private String stringValue;
    private ExcelColumnProperty currentProperty;
    private List<ExcelColumnProperty> columnPropertys = null;

    /**
     * 构造函数
     * 
     * @param columnPropertys ExcelColumnProperty集合
     * @param dataClass 数据类型
     */
    public EntityToExcelWriter(List<ExcelColumnProperty> columnPropertys) {
        this.columnPropertys = columnPropertys;
        propertySize = columnPropertys.size();
    }

    @Override
    public void fillRow(T rowData, Row excelSheetRow) {
        try {
            for (int cellIndex = 0; cellIndex < propertySize; cellIndex++) {
                currentCell = excelSheetRow.createCell(cellIndex);
                currentProperty = columnPropertys.get(cellIndex);

                currentValue = currentProperty.getPropertyDescriptor()
                        .getReadMethod()
                        .invoke(rowData);
                if (currentValue == null || StringUtils.EMPTY.equals(currentValue)) {
                    currentCell.setCellValue(StringUtils.EMPTY);
                    continue;
                }
                currentCell.setCellType(CellType.STRING);
                stringValue = ExcelConvertorSupport.convertToCellValue(currentProperty, currentValue);
                // 支持换行符
                if (stringValue.indexOf(CharUtils.LF) >= 0) {
                    currentCell.getCellStyle()
                            .setWrapText(true);
                    currentCell.setCellValue(new XSSFRichTextString(stringValue));
                } else {
                    currentCell.setCellValue(stringValue);
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ExcelAccessException(e);
        }

    }

}
