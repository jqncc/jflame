package org.jflame.commons.excel;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import org.jflame.commons.convert.CalendarToStringConverter;
import org.jflame.commons.convert.Converter;
import org.jflame.commons.convert.DateToStringConverter;
import org.jflame.commons.convert.NumberToStringConverter;
import org.jflame.commons.convert.ObjectToStringConverter;
import org.jflame.commons.convert.StringToBoolConverter;
import org.jflame.commons.convert.StringToDateConverter;
import org.jflame.commons.convert.StringToNumberConverter;
import org.jflame.commons.convert.StringToTemporalConverter;
import org.jflame.commons.convert.TemporalToStringConverter;
import org.jflame.commons.excel.convertor.BoolToTextConverter;
import org.jflame.commons.excel.convertor.DoubleToNumberConverter;
import org.jflame.commons.excel.handler.NullConverter;
import org.jflame.commons.exception.BusinessException;
import org.jflame.commons.exception.ConvertException;
import org.jflame.commons.reflect.BeanHelper;
import org.jflame.commons.util.NumberHelper;
import org.jflame.commons.util.StringHelper;

@SuppressWarnings("rawtypes")
public final class ExcelUtils {

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
            ObjectToStringConverter converter = getDefaultWriteConverter(valueClazz, property.getFmt());
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
                return new StringToBoolConverter();
            }
        } else if (valueClazz == Double.class || valueClazz == double.class) {
            if (needClazz == String.class) {
                if (StringHelper.isNotEmpty(fmt)) {
                    DecimalFormat numFormator = new DecimalFormat(fmt);
                    return new NumberToStringConverter(numFormator);
                } else {
                    return new NumberToStringConverter();
                }
            } else if (Number.class.isAssignableFrom(needClazz)) {
                return new DoubleToNumberConverter(needClazz);
            }
        } else if (valueClazz == String.class) {
            if (NumberHelper.isNumberType(needClazz)) {
                return new StringToNumberConverter((Class<Number>) needClazz);
            } else if (Date.class.isAssignableFrom(needClazz)) {
                return new StringToDateConverter(needClazz, fmt);
            } else if (Temporal.class.isAssignableFrom(needClazz)) {
                return new StringToTemporalConverter((Class<Temporal>) needClazz, fmt);
            } else if (needClazz == Boolean.class || needClazz == boolean.class) {
                return new StringToBoolConverter();
            }
        } else if (Date.class.isAssignableFrom(valueClazz)) {
            if (needClazz == String.class) {
                return new DateToStringConverter(fmt);
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
    public static <S> ObjectToStringConverter getDefaultWriteConverter(Class<S> valueClazz, String fmt) {
        // 数字如果有格式使用NumberToTextConverter,否则使用toString
        if (NumberHelper.isNumberType(valueClazz)) {
            if (StringHelper.isNotEmpty(fmt)) {
                DecimalFormat numFormator = new DecimalFormat(fmt);
                numFormator.setGroupingUsed(false);
                return new NumberToStringConverter(numFormator);
            } else {
                return new NumberToStringConverter();
            }
        } else if (boolean.class == valueClazz || Boolean.class == valueClazz) {
            return new BoolToTextConverter();
        } else if (java.util.Date.class.isAssignableFrom(valueClazz)) {
            return new DateToStringConverter(fmt);
        } else if (Temporal.class.isAssignableFrom(valueClazz)) {
            return new TemporalToStringConverter(fmt);
        } else if (valueClazz == Calendar.class) {
            return new CalendarToStringConverter(fmt);
        }
        return new ObjectToStringConverter<S>();
    }

    /**
     * 读取excel单元格的值.读取出来的值类型只可能是:bool,number,date,string.
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
        } else if (curCell.getCellType() == CellType.BLANK) {
            return StringUtils.EMPTY;
        } else {
            return curCell.toString();
        }
    }

    /**
     * 根据excel column获取bean的属性.
     * 
     * @param dataClass Class&lt;? extends IExcelEntity&gt;
     * @return excel column注解属性
     */
    public static List<ExcelColumnProperty> resolveExcelColumnProperty(Class<? extends IExcelEntity> dataClass,
            boolean isWrite) {
        List<ExcelColumnProperty> as = new ArrayList<ExcelColumnProperty>();

        final Class<ExcelColumn> clazz = ExcelColumn.class;
        final String clazzName = "class";

        ExcelColumnProperty newProperty;
        ExcelColumn tmpAnns;
        Field tmpField;
        Method tmpReadMethod;
        PropertyDescriptor[] properties = BeanHelper.getPropertyDescriptors(dataClass);
        for (PropertyDescriptor propDesc : properties) {
            if (clazzName.equals(propDesc.getName())) {
                continue;
            }
            tmpAnns = null;
            tmpReadMethod = propDesc.getReadMethod();
            if (tmpReadMethod != null && tmpReadMethod.isAnnotationPresent(clazz)) {
                tmpAnns = tmpReadMethod.getAnnotation(clazz);
            }
            if (tmpAnns == null) {
                tmpField = FieldUtils.getField(dataClass, propDesc.getName(), true);
                if (tmpField != null && tmpField.isAnnotationPresent(clazz)) {
                    tmpAnns = tmpField.getAnnotation(clazz);
                }
            }
            if (tmpAnns != null) {
                newProperty = new ExcelColumnProperty();
                newProperty.setPropertyDescriptor(propDesc);
                newProperty.setOrder(tmpAnns.order());
                // newProperty.setConvert(tmpAnns.convert());
                newProperty.setFmt(tmpAnns.fmt());
                newProperty.setName(tmpAnns.name());
                newProperty.setWidth(tmpAnns.width());
                setConverter(isWrite, newProperty, tmpAnns);

                as.add(newProperty);
            }
        }
        Collections.sort(as);
        return as;
    }

    private static void setConverter(boolean isWrite, ExcelColumnProperty newProperty, ExcelColumn tmpAnns) {
        if (isWrite) {
            if (tmpAnns.writeConverter() != NullConverter.class) {
                ObjectToStringConverter converter = null;
                if (StringHelper.isNotEmpty(tmpAnns.fmt())) {
                    try {
                        converter = tmpAnns.writeConverter()
                                .getConstructor(String.class)
                                .newInstance(tmpAnns.fmt());
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        e.printStackTrace();
                    }
                }
                if (converter == null) {
                    try {
                        converter = tmpAnns.writeConverter()
                                .newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new BusinessException(e);
                    }
                }
                newProperty.setWriteConverter(converter);
            }
        } else {
            if (tmpAnns.readConverter() != NullConverter.class) {
                Converter<?,?> converter = null;
                if (StringHelper.isNotEmpty(tmpAnns.fmt())) {
                    try {
                        converter = tmpAnns.readConverter()
                                .getConstructor(String.class)
                                .newInstance(tmpAnns.fmt());
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        e.printStackTrace();
                    }
                }
                if (converter == null) {
                    try {
                        converter = tmpAnns.readConverter()
                                .newInstance();

                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new BusinessException(e);
                    }
                }
                newProperty.setReadConverter(converter);
            }
        }
    }
}
