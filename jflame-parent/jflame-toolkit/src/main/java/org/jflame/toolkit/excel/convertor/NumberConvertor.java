package org.jflame.toolkit.excel.convertor;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.util.NumberHelper;
import org.jflame.toolkit.util.StringHelper;

/**
 * excel值与double型转换器
 * 
 * @author yucan.zhang
 */
public class NumberConvertor implements ICellValueConvertor<Number> {

    private Class<? extends Number> numberClass;

    public NumberConvertor(Class<? extends Number> realClass) {
        this.numberClass = realClass;
    }

    @Override
    public Number convertFromExcel(Object cellValue, final String fmt) throws ConvertException {
        if (cellValue == null) {
            return null;
        }
        if ((cellValue instanceof Double)) {
            return (Double) cellValue;
        }
        String text = StringUtils.trimToEmpty(cellValue.toString());
        try {
            if (StringUtils.isNotEmpty(fmt)) {
                DecimalFormat formator = new DecimalFormat();
                formator.setParseBigDecimal(true);
                formator.applyPattern(fmt);
                formator.setGroupingUsed(false);

                Number number = formator.parse(text);
                return NumberHelper.convertNumberToSubclass(number, this.numberClass);
            } else {
                return NumberHelper.parseNumber(text, numberClass);
            }
        } catch (ParseException e) {
            throw new ConvertException("使用格式:" + fmt + "转换失败", e);
        }
    }

    @Override
    public String convertToExcel(final Number value, final String fmt) throws ConvertException {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        DecimalFormat formator = new DecimalFormat();
        formator.setGroupingUsed(false);
        try {
            if (StringHelper.isNotEmpty(fmt)) {
                formator.applyPattern(fmt);
            }
            return formator.format(value);
        } catch (IllegalArgumentException e) {
            throw new ConvertException("使用格式:" + fmt + "转换失败", e);
        }
    }

    @Override
    public String getConvertorName() {
        return CellConvertorEnum.number.name();
    }
}
