package org.jflame.toolkit.valid;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.jflame.toolkit.valid.Digit.List;

/**
 * 数字验证注解,支持类型float,double,BigDecimal
 * 
 * @author yucan.zhang
 */
@Constraint(validatedBy = { DigitValidator.class })
@Documented
@Repeatable(List.class)
@Target({ METHOD,FIELD,ANNOTATION_TYPE,CONSTRUCTOR,PARAMETER,TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Digit {

    /**
     * 错误描述,如需引用params中的动态参数,使用{0},{1}占用符
     * 
     * @return
     */
    String message();

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
     * 整数最大位数,默认不限制
     * 
     * @return
     */
    int integer() default -1;

    /**
     * 最小小数位数,默认为0
     * 
     * @return
     */
    int minScale() default 0;

    /**
     * 最大小数位数,默认不限制
     * 
     * @return
     */
    int maxScale() default 0;

    /**
     * 最小值
     * 
     * @return
     */
    String min() default "";

    /**
     * 最大值
     * 
     * @return
     */
    String max() default "";

    @Target({ METHOD,FIELD,ANNOTATION_TYPE,CONSTRUCTOR,PARAMETER,TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    @interface List {

        Digit[] value();
    }

}
