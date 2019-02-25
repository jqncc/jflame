package org.jflame.toolkit.valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.toolkit.valid.DynamicValid.ValidRule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

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
            paramMap = new HashMap<>();
            if (rules.length == 1 && constraintAnnotation.params()
                    .indexOf(":") == 0) {
                paramMap.put(rules[0].name(), new String[] { constraintAnnotation.params() });
            } else {
                if (StringHelper.isNotEmpty(constraintAnnotation.params())) {
                    String paramText = StringUtils.deleteWhitespace(constraintAnnotation.params());
                    if (paramText.charAt(0) != '{') {
                        paramText = '{' + paramText;
                    }
                    if (!paramText.endsWith("}")) {
                        paramText = paramText + '}';
                    }
                    JSONObject tempMap = JSON.parseObject(paramText, Feature.AllowUnQuotedFieldNames);
                    for (Entry<String,Object> kv : tempMap.entrySet()) {
                        if (kv.getValue() instanceof JSONObject) {
                            String v = ((JSONObject) kv.getValue()).getString(kv.getKey());
                            paramMap.put(kv.getKey(), new String[] { v });
                        } else if (kv.getValue() instanceof JSONArray) {
                            List<String> list = ((JSONArray) kv.getValue()).toJavaList(String.class);
                            paramMap.put(kv.getKey(), CollectionHelper.toArray(list));
                        } else {
                            paramMap.put(kv.getKey(), new String[] { String.valueOf(kv.getValue()) });
                        }
                    }
                }
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

    /* public static void main(String[] args) {
        String x = "{size:1, min:[1,2]}";
        // String x = "{regex:\"[0-9]{1,3}\"}";
    
        String paramText = StringUtils.deleteWhitespace(x);
        if (paramText.charAt(0) != '{') {
            paramText = '{' + paramText;
        }
        if (!paramText.endsWith("}")) {
            paramText = paramText + '}';
        }
        Map<String,String[]> paramMap = new HashMap<>();
        //
        JSONObject tempMap = JSON.parseObject(paramText, Feature.AllowSingleQuotes);
    
        // Map<String,String> tempMap = JSON.parseObject(paramText, type, ~SerializerFeature.QuoteFieldNames.mask);
        for (Entry<String,Object> kv : tempMap.entrySet()) {
            if (kv.getValue() instanceof JSONObject) {
                String v = ((JSONObject) kv.getValue()).getString(kv.getKey());
                paramMap.put(kv.getKey(), new String[] { v });
            } else if (kv.getValue() instanceof JSONArray) {
                List<String> list = ((JSONArray) kv.getValue()).toJavaList(String.class);
                paramMap.put(kv.getKey(), CollectionHelper.toArray(list));
            } else {
                paramMap.put(kv.getKey(), new String[] { String.valueOf(kv.getValue()) });
            }
        }
        System.out.println(paramMap);
    }*/
}
