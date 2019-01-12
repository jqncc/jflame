package org.jflame.toolkit.excel.convertor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.jflame.toolkit.excel.ExcelColumnProperty;
import org.jflame.toolkit.excel.convertor.ICellValueConvertor.CellConvertorEnum;
import org.jflame.toolkit.exception.ConvertException;

@SuppressWarnings("rawtypes")
public final class ExcelConvertorSupport {

    private static Map<String,ICellValueConvertor> customConvertors = new ConcurrentHashMap<>();
    private static Map<Class,ICellValueConvertor> defaultConvertors = new HashMap<>();

    static {
        // 注册默认转换器
        defaultConvertors.put(String.class, new StringConvertor());
        defaultConvertors.put(boolean.class, new BoolConvertor());
        defaultConvertors.put(Boolean.class, new BoolConvertor());
        defaultConvertors.put(byte.class, new NumberConvertor(byte.class));
        defaultConvertors.put(Byte.class, new NumberConvertor(Byte.class));
        defaultConvertors.put(short.class, new NumberConvertor(short.class));
        defaultConvertors.put(Short.class, new NumberConvertor(Short.class));
        defaultConvertors.put(int.class, new NumberConvertor(int.class));
        defaultConvertors.put(Integer.class, new NumberConvertor(Integer.class));
        defaultConvertors.put(long.class, new NumberConvertor(long.class));
        defaultConvertors.put(Long.class, new NumberConvertor(Long.class));
        defaultConvertors.put(float.class, new NumberConvertor(float.class));
        defaultConvertors.put(Float.class, new NumberConvertor(Float.class));
        defaultConvertors.put(double.class, new NumberConvertor(double.class));
        defaultConvertors.put(Double.class, new NumberConvertor(Double.class));
        defaultConvertors.put(BigDecimal.class, new NumberConvertor(BigDecimal.class));
        defaultConvertors.put(BigInteger.class, new NumberConvertor(BigInteger.class));
        defaultConvertors.put(java.sql.Date.class, new DateConvertor(java.sql.Date.class));
        defaultConvertors.put(java.util.Date.class, new DateConvertor(java.util.Date.class));
        defaultConvertors.put(java.sql.Timestamp.class, new DateConvertor(java.sql.Timestamp.class));
        defaultConvertors.put(Calendar.class, new CalendarConvertor());
    }

    /**
     * 注册自定义转换器
     * 
     * @param convertors 值转换器
     */
    public static void registerConvertor(final ICellValueConvertor... convertors) {
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
    public static ICellValueConvertor getConvertor(final String convertorName) {
        ICellValueConvertor convertor = customConvertors.get(convertorName);
        if (convertor == null) {
            for (ICellValueConvertor c : defaultConvertors.values()) {
                if (!c.getConvertorName()
                        .equals(CellConvertorEnum.none.name())
                        && c.getConvertorName()
                                .equals(convertorName)) {
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
    public static ICellValueConvertor getDefaultConvertor(Class propertyClass) {
        return defaultConvertors.get(propertyClass);
    }

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
        ICellValueConvertor convertor = null;
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
                : String.valueOf(value);
    }

    /**
     * 转换excel单元格值到java属性,转换器或java属性类型必须指定一个
     * 
     * @param property ExcelColumnProperty
     * @param cell excel单元格
     * @return java属性值
     */
    public static Object extractFromCellValue(final ExcelColumnProperty property, final Cell cell) {
        if (cell == null) {
            return null;
        }
        ICellValueConvertor convertor;
        Object cellValue = getCellValue(cell);
        if (StringUtils.isNotEmpty(property.getConvert()) && !CellConvertorEnum.none.name()
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
        return cellValue;
    }

    /**
     * @param curCell
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
            return curCell.getStringCellValue();
        }
    }

}
