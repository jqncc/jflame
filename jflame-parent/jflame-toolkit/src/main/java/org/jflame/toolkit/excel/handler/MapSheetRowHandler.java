package org.jflame.toolkit.excel.handler;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 * excel单行数据与LinkedHashMap转换处理器. <br>
 * 注：使用有序的LinkedHashMap
 * 
 * @author yucan.zhang
 */
public class MapSheetRowHandler implements ISheetRowHandler<LinkedHashMap<String,Object>> {

    private String[] excludeKeys;// 需要排除的键,即不写入excel
    private String[] importKeys;

    public MapSheetRowHandler() {
    }

    /**
     * 构造
     * 
     * @param keys 导入时的map keys,顺序跟excel列一致
     */
    public MapSheetRowHandler(String[] keys) {
        if (keys == null || keys.length == 0) {
            throw new ExcelAccessException("未指定要导入的map keys");
        }
        importKeys = keys;
    }

    private int cellIndex;
    private Cell cell;
    private Entry<String,Object> entry;
    private Iterator<Entry<String,Object>> it;

    @Override
    public void fillRow(LinkedHashMap<String,Object> rowData, Row excelSheetRow) {
        it = rowData.entrySet()
                .iterator();
        cellIndex = 0;
        while (it.hasNext()) {
            entry = it.next();
            if (!isExcude(entry.getKey())) {
                cell = excelSheetRow.createCell(cellIndex++);
                cell.setCellType(CellType.STRING);
                cell.getCellStyle()
                        .setWrapText(true);
                // cell.setCellValue(ExcelConvertorSupport.convertToCellValue(null, entry.getValue()));
                String cellValue = ExcelConvertorSupport.convertToCellValue(null, entry.getValue());
                if (cellValue.indexOf(CharUtils.LF) >= 0) {
                    cell.setCellValue(new XSSFRichTextString(cellValue));
                } else {
                    cell.setCellValue(cellValue);
                }
            }
        }
    }

    private LinkedHashMap<String,Object> dataMap = null;

    @Override
    public LinkedHashMap<String,Object> extractRow(Row excelSheetRow) {
        if (importKeys == null || importKeys.length == 0) {
            throw new ExcelAccessException("未指定要导入的map keys");
        }
        dataMap = new LinkedHashMap<>();
        for (cellIndex = 0; cellIndex < importKeys.length; cellIndex++) {
            dataMap.put(importKeys[cellIndex], ExcelConvertorSupport
                    .getCellValue(excelSheetRow.getCell(cellIndex, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK)));
        }
        return dataMap;
    }

    boolean isExcude(String key) {
        if (ArrayUtils.isEmpty(excludeKeys)) {
            return false;
        } else {
            return ArrayUtils.contains(excludeKeys, key);
        }
    }

    public void setExcludeKeys(String[] excludeKeys) {
        this.excludeKeys = excludeKeys;
    }

}
