package org.jflame.toolkit.excel.handler;

import org.apache.poi.ss.usermodel.Row;

/**
 * excel sheet行与对象数据间转换处理接口
 * 
 * @author yucan.zhang
 * @param <T> 单行对应的数据类型
 */
public interface ISheetRowHandler<T> {

    /**
     * 对象数据对填充excel行.
     * 
     * @param rowData 对象数据
     * @param excelSheetRow excel行
     * @see org.apache.poi.ss.usermodel.Row
     */
    public void fillRow(T rowData, Row excelSheetRow);

    /**
     * excel行转对象数据类型
     * 
     * @param excelSheetRow excel行
     * @see org.apache.poi.ss.usermodel.Row
     * @return 对象数据
     */
    public T extractRow(Row excelSheetRow);
}
