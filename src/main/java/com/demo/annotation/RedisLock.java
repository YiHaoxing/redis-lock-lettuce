package com.demo.annotation;

import java.lang.annotation.*;

/**
 * @author YiHaoXing
 * @description Redis锁注解
 * @date 23:53 2019/6/28
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {
    /**
     * 锁的过期时间
     */
    int expireTime() default 1000;

    String key() default "";

    String value() default "";
}
