package org.jflame.commons.valid;

import java.math.BigDecimal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DigitValidator implements ConstraintValidator<Digit,Number> {

    private int maxIntegerLength;
    private int minFractionLength;
    private int maxFractionLength;
    private String minValue;
    private String maxValue;

    @Override
    public void initialize(Digit digit) {
        this.maxIntegerLength = digit.integer();
        if (this.maxIntegerLength < -1) {
            this.maxIntegerLength = -1;
        }
        this.minFractionLength = digit.minScale();
        this.maxFractionLength = digit.maxScale();
        if (this.maxFractionLength < -1) {
            this.maxFractionLength = -1;
        }
        this.minValue = digit.min();
        this.maxValue = digit.max();
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BigDecimal bigValue;
        if (value instanceof BigDecimal) {
            bigValue = (BigDecimal) value;
        } else {
            bigValue = new BigDecimal(value.toString()).stripTrailingZeros();
        }

        int integerPartLength = bigValue.precision() - bigValue.scale();
        int fractionPartLength = bigValue.scale() < 0 ? 0 : bigValue.scale();
        if (maxIntegerLength > -1 && maxIntegerLength < integerPartLength) {
            return false;
        }
        if (minFractionLength > fractionPartLength) {
            return false;
        }
        if (maxFractionLength > -1 && maxFractionLength < fractionPartLength) {
            return false;
        }
        if (!"".equals(maxValue)) {
            BigDecimal max = new BigDecimal(maxValue);
            if (bigValue.compareTo(max) > 0) {
                return false;
            }
        }

        if (!"".equals(minValue)) {
            BigDecimal min = new BigDecimal(minValue);
            if (bigValue.compareTo(min) < 0) {
                return false;
            }
        }

        return true;
    }

}
