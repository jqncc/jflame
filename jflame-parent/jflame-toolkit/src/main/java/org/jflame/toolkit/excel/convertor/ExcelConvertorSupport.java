package org.jflame.toolkit.excel.convertor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.excel.convertor.ICellValueConvertor.CellConvertorEnum;
import org.jflame.toolkit.exception.ConvertException;

@SuppressWarnings("rawtypes")
public class ExcelConvertorSupport {

    private Map<String,ICellValueConvertor> customConvertors = new ConcurrentHashMap<>();
    private Map<Class,ICellValueConvertor> defaultConvertors = new HashMap<>();

    private static final ExcelConvertorSupport instance = new ExcelConvertorSupport();

    private ExcelConvertorSupport() {
        // 注册默认转换器
        defaultConvertors.put(boolean.class, new BoolConvertor());
        defaultConvertors.put(Boolean.class, new BoolConvertor());
        defaultConvertors.put(byte.class, new NumberConvertor(Byte.class));
        defaultConvertors.put(short.class, new NumberConvertor(Short.class));
        defaultConvertors.put(int.class, new NumberConvertor(Integer.class));
        defaultConvertors.put(long.class, new NumberConvertor(Long.class));
        defaultConvertors.put(float.class, new NumberConvertor(Float.class));
        defaultConvertors.put(double.class, new NumberConvertor(Double.class));
        defaultConvertors.put(BigDecimal.class, new NumberConvertor(BigDecimal.class));
        defaultConvertors.put(BigInteger.class, new NumberConvertor(BigInteger.class));
        defaultConvertors.put(java.sql.Date.class, new DateConvertor(java.sql.Date.class));
        defaultConvertors.put(java.util.Date.class, new DateConvertor(java.util.Date.class));
        defaultConvertors.put(java.sql.Timestamp.class, new DateConvertor(java.sql.Timestamp.class));
    }

    public static ExcelConvertorSupport getInstance() {
        return instance;
    }

    /**
     * 注册自定义转换器
     * 
     * @param convertors 值转换器
     */
    public void registerConvertor(final ICellValueConvertor... convertors) {
        for (ICellValueConvertor c : convertors) {
            if (!EnumUtils.isValidEnum(CellConvertorEnum.class, c.getConvertorName())) {
                if (!customConvertors.containsKey(c.getConvertorName())) {
                    customConvertors.put(c.getConvertorName(), c);
                }
            } else {
                throw new IllegalArgumentException("不允许使用的名称" + c.getConvertorName());
            }
        }
    }

    /**
     * 根据名称获取转换器.包括默认转换器
     * 
     * @param convertorName 转换器名称
     * @return ICellValueConvertor
     */
    public ICellValueConvertor getConvertor(final String convertorName) {
        ICellValueConvertor convertor = customConvertors.get(convertorName);
        if (convertor == null) {
            for (ICellValueConvertor c : defaultConvertors.values()) {
                if (!c.getConvertorName().equals(CellConvertorEnum.NONE.toString())
                        && c.getConvertorName().equals(convertorName)) {
                    convertor = c;
                }
            }

        }
        return convertor;
    }

    /**
     * 获取内置转换器
     * 
     * @param propertyClass 属性类型
     * @return ICellValueConvertor
     */
    public ICellValueConvertor getDefaultConvertor(Class propertyClass) {
        return defaultConvertors.get(propertyClass);
    }

    /**
     * 使用指定名称转换器，将java属性值转为excel单元格值,未找到转换器返回toString()
     * 
     * @param convertorName 转换器名称
     * @param value excel单元格值
     * @param format 格式
     * @throws ConvertException 转换器未找到或转换异常
     * @return 转换后字符串
     */
    @SuppressWarnings({ "unchecked" })
    public static String convertToCellValue(final String convertorName, final Object value, final String format) {
        ExcelConvertorSupport convertorSupport = ExcelConvertorSupport.getInstance();
        ICellValueConvertor convertor;
        if (StringUtils.isNotEmpty(convertorName)) {
            convertor = convertorSupport.getConvertor(convertorName);
            if (convertor != null) {
                return convertor.convertToExcel(value, format);
            }
        } else {
            Class valueClazz = value.getClass();
            Object newValue = null;
            if (valueClazz == Calendar.class) {
                valueClazz = Date.class;
                newValue = ((Calendar) value).getTime();
            }
            convertor = convertorSupport.getDefaultConvertor(valueClazz);
            if (convertor != null) {
                return convertor.convertToExcel(newValue == null ? value : newValue, format);
            }
        }
        return String.valueOf(value);
    }

    /**
     * 转换excel单元格值到java属性,转换器或java属性类型必须指定一个
     * 
     * @param convertorName 转换器名称
     * @param propertyClass java属性类型
     * @param fmt 格式
     * @param cellValue excel单元格值
     * @return java属性值
     */
    public static Object convertValueFromCellValue(final String convertorName, final Class<?> propertyClass,
            final String fmt, final Object cellValue) {
        if (String.class.equals(propertyClass)) {
            if (cellValue instanceof String) {
                return cellValue;
            }
            return cellValue.toString();
        }
        ExcelConvertorSupport convertorSupport = ExcelConvertorSupport.getInstance();
        ICellValueConvertor convertor;
        if (StringUtils.isNotEmpty(convertorName)) {
            convertor = convertorSupport.getConvertor(convertorName);
            if (convertor != null) {
                return convertor.convertFromExcel(cellValue, fmt);
            }
        }
        if (propertyClass != null) {
            convertor = convertorSupport.getDefaultConvertor(propertyClass);
            if (convertor != null) {
                return convertor.convertFromExcel(cellValue, fmt);
            }
        }

        throw new ConvertException("没有找到类型" + propertyClass.getName() + "的值转换器");
    }
}
