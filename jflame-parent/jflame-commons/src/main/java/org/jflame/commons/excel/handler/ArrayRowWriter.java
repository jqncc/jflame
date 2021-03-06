package org.jflame.commons.excel.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import org.jflame.commons.convert.ObjectToStringConverter;
import org.jflame.commons.excel.ExcelConvertUtils;

@SuppressWarnings("rawtypes")
public class ArrayRowWriter implements IExcelRowWriter<Object[]> {

    private Cell cell;
    private Object currentValue;
    private Map<Integer,ObjectToStringConverter> columnConvertMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void fillRow(Object[] rowData, Row excelSheetRow) {
        if (ArrayUtils.isEmpty(rowData)) {
            return;
        }
        ObjectToStringConverter converter = null;
        String cellValue;
        for (int cellIndex = 0; cellIndex < rowData.length; cellIndex++) {
            cell = excelSheetRow.createCell(cellIndex);
            cell.setCellType(CellType.STRING);
            currentValue = rowData[cellIndex];
            if (currentValue != null) {
                if (columnConvertMap.containsKey(cellIndex)) {
                    converter = columnConvertMap.get(cellIndex);
                } else {
                    converter = ExcelConvertUtils.getDefaultWriteConverter(currentValue.getClass(), null);
                    columnConvertMap.put(cellIndex, converter);
                }

                cellValue = converter.convert(currentValue);
                if (cellValue.indexOf(CharUtils.LF) >= 0) {
                    cell.getCellStyle()
                            .setWrapText(true);
                    cell.setCellValue(new XSSFRichTextString(cellValue));
                } else {
                    cell.setCellValue(cellValue);
                }
            }
        }
    }

}
