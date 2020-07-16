package org.jflame.commons.valid;

import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.util.StringHelper;
import org.jflame.commons.valid.annotation.DynamicValid;
import org.jflame.commons.valid.annotation.DynamicValid.ValidRule;

/**
 * 使用指定的内置验证规则验证
 * 
 * @see DynamicValid
 * @author yucan.zhang
 */
public class DynamicValidator implements ConstraintValidator<DynamicValid,String> {

    private ValidRule[] rules;
    private boolean nullable = true;
    private Map<String,String[]> paramMap;

    @Override
    public void initialize(DynamicValid constraintAnnotation) {
        rules = constraintAnnotation.rules();
        nullable = constraintAnnotation.nullable();
        if (constraintAnnotation.params()
                .length() > 1) {
            if (rules.length == 1 && constraintAnnotation.params()
                    .indexOf(":") < 0) {
                paramMap = new HashMap<>();
                paramMap.put(rules[0].name(),
                        parseParamValue(StringUtils.deleteWhitespace(constraintAnnotation.params())));
            } else {

            }
        }

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean flag = false;

        if (StringHelper.isEmpty(value)) {
            return nullable;
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
                case minLen:
                    flag = ValidatorHelper.minLength(value, Integer.parseInt(params[0]));
                    break;
                case maxLen:
                    flag = ValidatorHelper.maxLength(value, Integer.parseInt(params[0]));
                    break;
                case size:
                    flag = ValidatorHelper.stringLength(value, Integer.parseInt(params[0]),
                            Integer.parseInt(params[1]));
                    break;
                case regex:
                    flag = ValidatorHelper.regex(value, params[0]);
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

    /**
     * 解析验证规则的参数.参数格式:规则名1:参数1;规则名2:[数组参数1,数组参数2],多个规则时参数中不能含;号
     * 
     * @param paramText
     * @return
     */
    static Map<String,String[]> parseParam(String paramText) {
        Map<String,String[]> paramMap = new HashMap<>();
        String[] paramArr = StringUtils.deleteWhitespace(paramText)
                .split(";");
        String[] tmpArr;
        String[] tmpValueArr;
        for (String paramKv : paramArr) {
            tmpArr = paramKv.split(":");
            tmpValueArr = parseParamValue(tmpArr[1]);
            paramMap.put(tmpArr[0], tmpValueArr);
        }

        return paramMap;
    }

    static String[] parseParamValue(String paramValue) {
        if (paramValue.charAt(0) == '\'') {
            char[] tmpChars = paramValue.toCharArray();
            return new String[] { String.valueOf(tmpChars, 1, tmpChars.length - 2) };
        } else if (paramValue.charAt(0) == '[') {
            char[] tmpChars = paramValue.toCharArray();
            return String.valueOf(tmpChars, 1, tmpChars.length - 2)
                    .split(",");
        } else {
            return new String[] { paramValue };
        }
    }

    /*public static void main(String[] args) {
        Map<String,String[]> m1 = parseParam("min:1;size:[2,3]");
        m1.forEach((k, v) -> {
            System.out.println(k + "=" + Arrays.toString(v));
        });
    
        Map<String,String[]> m2 = parseParam("min:1;noContain:'%88*'");
        m2.forEach((k, v) -> {
            System.out.println(k + "=" + Arrays.toString(v));
        });
    }*/
}
