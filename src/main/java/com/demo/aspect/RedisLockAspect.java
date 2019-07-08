package com.demo.aspect;

import com.demo.annotation.RedisLock;
import com.demo.redis.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author YiHaoXing
 * @description
 * @date 0:06 2019/6/29
 **/
@Aspect
@Component
@Slf4j
public class RedisLockAspect {
    @Autowired
    private RedisLockUtils redisLockUtils;

    @Pointcut("@annotation(com.demo.annotation.RedisLock)")
    public void redisLockPointCut() {
    }


    @Around("redisLockPointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        RedisLock annotation = method.getAnnotation(RedisLock.class);

        String key = annotation.key();
        String value = annotation.value();
        int expireTime = annotation.expireTime();

        //获取锁
        boolean lock = redisLockUtils.getLock(key, value, expireTime);
        if (lock) {
            try {
                return proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                throw throwable;
            } finally {
                //释放锁
                redisLockUtils.releaseLockByLua(key, value);
            }
        }
        return null;
    }
}
