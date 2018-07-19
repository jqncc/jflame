package org.jflame.toolkit.excel.handler;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.jflame.toolkit.excel.ExcelColumnProperty;
import org.jflame.toolkit.excel.IExcelEntity;

public abstract class BaseEntitySheetRowHandler<T extends IExcelEntity> implements ISheetRowHandler<T> {

    /**
     * 获取该bean的所有标注了excelColumn注解的属性
     * 
     * @return
     */
    // public abstract PropertyDescriptor[] getProperties();

    /**
     * 获取ExcelColumnProperty
     * 
     * @return
     */
    public abstract List<ExcelColumnProperty> getColumnPropertys();

    public abstract void fillRow(T rowData, Row excelSheetRow);

}
