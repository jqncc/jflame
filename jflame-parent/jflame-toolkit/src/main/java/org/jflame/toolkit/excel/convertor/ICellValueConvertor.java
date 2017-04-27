package org.jflame.toolkit.excel.convertor;

import org.jflame.toolkit.exception.ConvertException;

/**
 * excel单元格值与类型转换接口
 * 
 * @author zyc
 */
public interface ICellValueConvertor<T> {

    /**
     * excel单元格值转为java类型值
     * 
     * @param cellValue excel单元格值
     * @param pattern 格式
     * @return 转换后的值
     * @throws ConvertException 值转换异常
     */
    public T convertFromExcel(Object cellValue, String pattern) throws ConvertException;

    /**
     * java类型值转为excel单元格字符值
     * 
     * @param value 数据
     * @param pattern 格式字符串
     * @return 转换后字符串
     * @throws ConvertException 值转换异常
     */
    public String convertToExcel(final T value, final String pattern) throws ConvertException;

    /**
     * 该转换器唯一名称
     * 
     * @return
     */
    public String getConvertorName();

    /**
     * 内置的转换器名称
     * 
     * @author yucan.zhang
     */
    public enum CellConvertorEnum {
        none,bool, date, number,string;
    }
}
