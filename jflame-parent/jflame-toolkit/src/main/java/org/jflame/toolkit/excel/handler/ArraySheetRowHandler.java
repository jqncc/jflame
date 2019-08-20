package org.jflame.toolkit.excel.handler;

import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class ArraySheetRowHandler implements ISheetRowHandler<Object[]> {

    private Cell cell;
    private int cellIndex;

    @Override
    public void fillRow(Object[] rowData, Row excelSheetRow) {
        if (ArrayUtils.isEmpty(rowData)) {
            return;
        }
        for (cellIndex = 0; cellIndex < rowData.length; cellIndex++) {
            cell = excelSheetRow.createCell(cellIndex);
            cell.setCellType(CellType.STRING);
            cell.getCellStyle()
                    .setWrapText(true);
            String cellValue = ExcelConvertorSupport.convertToCellValue(null, rowData[cellIndex]);
            if (cellValue.indexOf(CharUtils.LF) >= 0) {
                cell.setCellValue(new XSSFRichTextString(cellValue));
            } else {
                cell.setCellValue(cellValue);
            }

        }
    }

    private int firstIndex;
    private int lastIndex;
    private Object[] newObjs;

    @Override
    public Object[] extractRow(Row excelSheetRow) {
        firstIndex = excelSheetRow.getFirstCellNum();
        lastIndex = excelSheetRow.getLastCellNum();
        newObjs = new Object[lastIndex - firstIndex];
        for (int i = firstIndex, j = 0; i < lastIndex; i++, j++) {
            newObjs[j] = ExcelConvertorSupport.getCellValue(excelSheetRow.getCell(i));
        }
        return newObjs;
    }

}
