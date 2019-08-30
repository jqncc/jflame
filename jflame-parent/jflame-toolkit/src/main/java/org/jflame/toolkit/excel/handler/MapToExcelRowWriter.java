package org.jflame.toolkit.excel.handler;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jflame.toolkit.convert.ObjectToTextConverter;
import org.jflame.toolkit.excel.ExcelUtils;

import org.apache.commons.lang3.CharUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.jboss.netty.util.internal.ConcurrentHashMap;

/**
 * excel单行数据与LinkedHashMap转换处理器. <br>
 * 注：使用有序的LinkedHashMap
 * 
 * @author yucan.zhang
 */
@SuppressWarnings("rawtypes")
public class MapToExcelRowWriter implements IExcelRowWriter<LinkedHashMap<String,Object>> {

    private Cell cell;
    private Entry<String,Object> entry;
    private Iterator<Entry<String,Object>> it;

    private Map<Integer,ObjectToTextConverter> columnConvertMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void fillRow(LinkedHashMap<String,Object> rowData, Row excelSheetRow) {
        it = rowData.entrySet()
                .iterator();
        int cellIndex = 0;
        ObjectToTextConverter converter = null;
        String cellValue;
        while (it.hasNext()) {
            entry = it.next();

            cell = excelSheetRow.createCell(cellIndex);
            cell.setCellType(CellType.STRING);
            if (columnConvertMap.containsKey(cellIndex)) {
                converter = columnConvertMap.get(cellIndex);
            } else {
                converter = ExcelUtils.getDefaultWriteConverter(entry.getValue()
                        .getClass(), null);
                columnConvertMap.put(cellIndex, converter);
            }
            cellIndex++;
            cellValue = converter.convert(entry.getValue());
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
