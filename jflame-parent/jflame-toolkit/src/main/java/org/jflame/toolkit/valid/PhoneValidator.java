package org.jflame.toolkit.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jflame.toolkit.util.StringHelper;

/**
 * 手机或电话号验证器
 * @see Phone
 * @author yucan.zhang
 */
public class PhoneValidator implements ConstraintValidator<Phone,String> {

    private boolean isTestTel;
    private boolean isTestMobile;

    @Override
    public void initialize(Phone constraintAnnot) {
        isTestTel = constraintAnnot.testTel();
        isTestMobile = constraintAnnot.testMoble();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringHelper.isNotEmpty(value)) {
            if (isTestMobile && isTestTel) {
                return ValidatorHelper.isMobileOrTel(value, 2);
            } else {
                if (isTestMobile) {
                    return ValidatorHelper.isMobileOrTel(value, 0);
                }
                if (isTestTel) {
                    return ValidatorHelper.isMobileOrTel(value, 1);
                }
            }
        }
        return false;
    }

}
