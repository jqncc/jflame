package org.jflame.toolkit.excel;

import java.text.DecimalFormat;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.jflame.toolkit.convert.CalendarToTextConverter;
import org.jflame.toolkit.convert.Converter;
import org.jflame.toolkit.convert.DateToTextConverter;
import org.jflame.toolkit.convert.NumberToTextConverter;
import org.jflame.toolkit.convert.ObjectToTextConverter;
import org.jflame.toolkit.convert.TemporalToTextConverter;
import org.jflame.toolkit.convert.TextToBoolConverter;
import org.jflame.toolkit.convert.TextToDateConverter;
import org.jflame.toolkit.convert.TextToNumberConverterFactory;
import org.jflame.toolkit.convert.TextToTemporalConverterFactory;
import org.jflame.toolkit.excel.convertor.BoolToTextConverter;
import org.jflame.toolkit.excel.convertor.DoubleToNumberConverter;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.NumberHelper;
import org.jflame.toolkit.util.StringHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

@SuppressWarnings("rawtypes")
public final class ExcelConvertorSupport {

    /**
     * 使用指定名称转换器，将java属性值转为excel单元格值,未找到转换器返回toString()
     * 
     * @param property ExcelColumnProperty
     * @param value excel单元格值
     * @throws ConvertException 转换器未找到或转换异常
     * @return 转换后字符串
     */
    @SuppressWarnings({ "unchecked" })
    public static String convertToCellValue(final ExcelColumnProperty property, final Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        /* ICellValueConvertor convertor = null;
        if (property != null) {
            if (StringUtils.isNotEmpty(property.getConvert()) && !CellConvertorEnum.none.name()
                    .equals(property.getConvert())) {
                convertor = getConvertor(property.getConvert());
            }
            if (convertor == null) {
                convertor = getDefaultConvertor(property.getPropertyDescriptor()
                        .getPropertyType());
            }
        } else {
            convertor = getDefaultConvertor(value.getClass());
        }
        return convertor != null ? convertor.convertToExcel(value, property != null ? property.getFmt() : null)
                : String.valueOf(value);*/
        if (property.getWriteConverter() != null) {
            return property.getWriteConverter()
                    .convert(value);
        } else {
            Class<?> valueClazz = property.getPropertyDescriptor()
                    .getPropertyType();
            ObjectToTextConverter converter = getDefaultWriteConverter(valueClazz, property.getFmt());
            property.setWriteConverter(converter);// 转换器设置到ExcelColumnProperty,下一行数据直接用
            return converter.convert(value);
        }
    }

    /**
     * 转换excel单元格值到java属性
     * 
     * @param property ExcelColumnProperty
     * @param cell excel单元格
     * @return java属性值
     */
    @SuppressWarnings("unchecked")
    public static Object extractValueFromCell(final ExcelColumnProperty property, final Cell cell) {
        Object cellValue = getCellValue(cell);
        if (cell == null) {
            return null;
        }
        Class<?> valueClazz = cellValue.getClass();
        Class<?> needClazz = property.getPropertyDescriptor()
                .getPropertyType();
        // 从excel读取到的数据类型与要求的类型相同不需转换直接返回
        if (valueClazz == needClazz) {
            return cellValue;
        }
        if (property.getReadConverter() != null) {
            return property.getReadConverter()
                    .convert(cellValue);
        } else {
            Converter readConverter = getDefaultReadConverter(valueClazz, needClazz, property.getFmt());
            property.setReadConverter(readConverter);
            return readConverter.convert(cellValue);
        }
        /*if (StringUtils.isNotEmpty(property.getConvert()) && !CellConvertorEnum.none.name()
                .equals(property.getConvert())) {
            convertor = getConvertor(property.getConvert());
            if (convertor != null) {
                return convertor.convertFromExcel(cellValue, property.getFmt());
            }
        }
        convertor = getDefaultConvertor(property.getPropertyDescriptor()
                .getPropertyType());
        if (convertor != null) {
            return convertor.convertFromExcel(cellValue, property.getFmt());
        }
        return cellValue;*/
    }

    /**
     * 获取excel读取的值转为java属性的默认类型转换器
     * 
     * @param valueClazz 读取到的excel值类型,请看{@link #getCellValue}
     * @param needClazz
     * @param fmt
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Converter getDefaultReadConverter(Class<?> valueClazz, Class<?> needClazz, String fmt) {
        if (valueClazz == Boolean.class || valueClazz == boolean.class) {
            if (needClazz == String.class) {
                return new TextToBoolConverter();
            }
        } else if (valueClazz == Double.class || valueClazz == double.class) {
            if (needClazz == String.class) {
                if (StringHelper.isNotEmpty(fmt)) {
                    DecimalFormat numFormator = new DecimalFormat(fmt);
                    return new NumberToTextConverter(numFormator);
                } else {
                    return new NumberToTextConverter();
                }
            } else if (Number.class.isAssignableFrom(needClazz)) {
                return new DoubleToNumberConverter(needClazz);
            }
        } else if (valueClazz == String.class) {
            if (NumberHelper.isNumberType(needClazz)) {
                return new TextToNumberConverterFactory().getConverter((Class<Number>) needClazz);
            } else if (Date.class.isAssignableFrom(needClazz)) {
                return new TextToDateConverter(needClazz, Optional.ofNullable(fmt));
            } else if (Temporal.class.isAssignableFrom(needClazz)) {
                return new TextToTemporalConverterFactory(fmt).getConverter((Class<Temporal>) needClazz);
            } else if (needClazz == Boolean.class || needClazz == boolean.class) {
                return new TextToBoolConverter();
            }
        } else if (Date.class.isAssignableFrom(valueClazz)) {
            if (needClazz == String.class) {
                return new DateToTextConverter(fmt);
            }
        }
        throw new ConvertException("不支持的转换" + valueClazz + " to " + needClazz);
    }

    /**
     * 获取属性转为字符串的默认转换器
     * 
     * @param valueClazz 要转换的数据类型
     * @param fmt
     * @return
     */
    public static <S> ObjectToTextConverter getDefaultWriteConverter(Class<S> valueClazz, String fmt) {
        // 数字如果有格式使用NumberToTextConverter,否则使用toString
        if (NumberHelper.isNumberType(valueClazz)) {
            if (StringHelper.isNotEmpty(fmt)) {
                DecimalFormat numFormator = new DecimalFormat(fmt);
                numFormator.setGroupingUsed(false);
                return new NumberToTextConverter(numFormator);
            } else {
                return new NumberToTextConverter();
            }
        } else if (boolean.class == valueClazz || Boolean.class == valueClazz) {
            return new BoolToTextConverter();
        } else if (java.util.Date.class.isAssignableFrom(valueClazz)) {
            return new DateToTextConverter(fmt);
        } else if (Temporal.class.isAssignableFrom(valueClazz)) {
            return new TemporalToTextConverter(fmt);
        } else if (valueClazz == Calendar.class) {
            return new CalendarToTextConverter(fmt);
        }
        return new ObjectToTextConverter<S>();
    }

    /**
     * 读取excel单元格的值.读取出来的值类型只可能是:bool,number,date,string
     * 
     * @param curCell 单元格cell
     * @return
     */
    public static Object getCellValue(Cell curCell) {
        if (curCell == null) {
            return null;
        }
        if (curCell.getCellType() == CellType.BOOLEAN) {
            return curCell.getBooleanCellValue();
        } else if (curCell.getCellType() == CellType.NUMERIC) {
            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(curCell)) {
                return curCell.getDateCellValue();
            } else {
                return curCell.getNumericCellValue();
            }
        } else if (curCell.getCellType() == CellType.FORMULA) {
            return curCell.getNumericCellValue();// 公式类型取计算结果
        } else if (curCell.getCellType() == CellType._NONE) {
            return null;
        } else if (curCell.getCellType() == CellType.BLANK) {
            return StringUtils.EMPTY;
        } else {
            return curCell.getCellStyle()
                    .getWrapText()
                            ? curCell.getRichStringCellValue()
                                    .getString()
                            : curCell.getStringCellValue();
        }
    }

}
