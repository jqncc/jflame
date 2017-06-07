package org.jflame.db.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 数据表注解
 * 
 * @author yucan.zhang
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {

    /**
     * 表名
     * 
     * @return
     */
    String name() default "";
}
