package org.jflame.commons.valid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.jflame.commons.valid.DynamicValidator;

/**
 * 动态验证注解,使用指定的内置验证规则验证.<br>
 * 支持验证规则请查看:{@link ValidRule}
 * 
 * @see ValidRule
 * @author yucan.zhang
 */
@Constraint(validatedBy = { DynamicValidator.class })
@Documented
@Target({ ElementType.ANNOTATION_TYPE,ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicValid {

    /**
     * 错误描述,如需引用params中的动态参数,使用{0},{1}占用符
     * 
     * @return
     */
    String message();

    /**
     * 指定验证规则,多条规则是and关系
     * 
     * @return
     */
    ValidRule[] rules();

    /**
     * 校验分组
     * 
     * @return
     */
    Class<?>[] groups() default {};

    /**
     * 用于同一属性不同条件时的验证区分
     * 
     * @return
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 是否可用null 默认false
     * 
     * @return
     */
    boolean nullable() default false;

    /**
     * 规则所需参数. 参数格式:规则名1:参数1;规则名2:[数组参数1,数组参数2],<strong>多个规则时参数中不能含;号</strong><br>
     * 如:rules={ValidRule.min,ValidRule.regex,ValidRule.size} param="min:1,regex:'\\d+',size:[1,4]";<br>
     * 只有一个规则时,参数可以不带规则名,如rules=ValidRule.min,param="1"
     * 
     * @return
     */
    String params() default "";

    /**
     * 内置验证规则: 手机号,电话号,身份证,字母,字母,数字或下划线,ip地址,特殊字符,长度范围,正则等
     * 
     * @author yucan.zhang
     */
    public enum ValidRule {
        /**
         * 手机
         */
        mobile,
        /**
         * 电话号
         */
        tel,
        /**
         * 电话或手机号
         */
        mobileOrTel,
        /**
         * 身份证验证
         */
        idcard,
        /**
         * 字母
         */
        letter,
        /**
         * 字母,数字或下划线
         */
        letterNumOrline,
        /**
         * ip地址
         */
        ip,
        /**
         * 不包含特殊字符*%#\=&lt;&gt;`';?&amp;!
         */
        safeChar,
        noContain,
        /**
         * 长度范围
         */
        size,
        /**
         * 最小长度
         */
        minLen,
        /**
         * 最大长度
         */
        maxLen,
        regex
    }

}
