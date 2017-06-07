package org.jflame.db.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 数据表列
 * 
 * @author yucan.zhang
 */
@Target({ METHOD})
@Retention(RUNTIME)
public @interface Column {

    /**
     * 列名
     * 
     * @return
     */
    String name() default "";

    /**
     * 是否参与新增,false新增操作时将忽略该列
     * 
     * @return
     */
    boolean insertable() default true;

    /**
     * 是否参与更新,false更新操作时将忽略该列
     * 
     * @return
     */
    boolean updatable() default true;
}
