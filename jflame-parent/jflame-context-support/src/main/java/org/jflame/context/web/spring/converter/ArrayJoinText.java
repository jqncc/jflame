package org.jflame.context.web.spring.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注数组与逗号分隔的字符串间的转换.如[1,2,3] =&gt; 1,2,3
 * 
 * @author yucan.zhang
 */
@Target({ ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ArrayJoinText {

}
