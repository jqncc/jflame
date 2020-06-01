package org.jflame.context.spring.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface GlobalLock {

    /**
     * 锁名,可以是SPEL表达式
     * 
     * @return
     */
    String lockName();

    LockType lockType();

    /**
     * 过期时间,单位:秒s. 使用redis锁时必填
     * 
     * @return
     */
    int lockTime() default 5;

    /**
     * 获取锁的待时间,单位:毫秒ms. 使用redis锁时必填
     * 
     * @return
     */
    int waitTime() default 50;

    /**
     * 分布式锁类型
     * 
     * @author yucan.zhang
     */
    public enum LockType {
        zk,
        redis
    }
}
