package org.jflame.toolkit.valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.JsonHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.toolkit.valid.DynamicValid.ValidRule;

/**
 * 使用指定的内置验证规则验证
 * 
 * @see DynamicValid
 * @author yucan.zhang
 */
public class DynamicValidator implements ConstraintValidator<DynamicValid,String> {

    private ValidRule[] rules;
    private boolean nullable;
    private Map<String,String[]> paramMap;

    @Override
    public void initialize(DynamicValid constraintAnnotation) {
        rules = constraintAnnotation.rules();
        nullable = constraintAnnotation.nullable();
        if (constraintAnnotation.params().length() > 1) {
            if (rules.length == 1 && constraintAnnotation.params().indexOf(":") == 0) {
                paramMap.put(rules[0].name(), new String[]{ constraintAnnotation.params() });
            } else {
                Map<String,String> tempMap = new HashMap<>();
                tempMap = JsonHelper.parseMap(constraintAnnotation.params(), String.class, String.class);
                for (Entry<String,String> kv : tempMap.entrySet()) {
                    if (kv.getValue().charAt(0) == '[') {
                        paramMap.put(kv.getKey(),
                                CollectionHelper.toArray(JsonHelper.parseArray(kv.getValue(), String.class)));
                    } else {
                        paramMap.put(kv.getKey(), new String[]{ kv.getValue() });
                    }
                }
            }
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean flag = false;
        if (nullable && (value == null || "".equals(value))) {
            return true;
        }
        String[] params = null;
        for (ValidRule rule : rules) {
            flag = false;
            if (paramMap != null) {
                params = paramMap.get(rule.name());
            }
            switch (rule) {
                case mobile:
                    flag = ValidatorHelper.isMobileOrTel(value, 0);
                    break;
                case tel:
                    flag = ValidatorHelper.isMobileOrTel(value, 1);
                    break;
                case mobileOrTel:
                    flag = ValidatorHelper.isMobileOrTel(value, 2);
                    break;
                case idcard:
                    flag = ValidatorHelper.isIDCard(value);
                    break;
                case letter:
                    flag = ValidatorHelper.isLetter(value);
                    break;
                case letterNumOrline:
                    flag = ValidatorHelper.isLetterOrNumOrUnderline(value);
                    break;
                case ip:
                    flag = ValidatorHelper.isIPAddress(value);
                    break;
                case safeChar:
                    flag = ValidatorHelper.safeChar(value);
                    break;
                case noContain:
                    flag = !StringHelper.containsAny(value, params[0].split(","));
                    break;
                default:
                    break;
            }
            if (!flag) {
                break;
            }
        }
        return flag;
    }

}
