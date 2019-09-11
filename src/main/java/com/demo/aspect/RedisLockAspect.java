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

        //锁的key
        String key = annotation.value();
        //过期时间
        int expireTime = annotation.expireTime();
        //key对应的value.设置成这样可以避免释放掉其他线程加的锁.
        String value = new StringBuilder().append(Thread.currentThread().getId()).append(Math.random()).toString();

        boolean lock = false;
        try {
            //获取锁
            lock = redisLockUtils.getLock(key, value, expireTime);
            if (lock) {
                log.info("Thread:{}获取锁成功",Thread.currentThread().getId());
                return proceedingJoinPoint.proceed();
            } else {
                log.info("Thread:{}获取锁失败",Thread.currentThread().getId());
            }
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            //释放锁
            if(lock){
                redisLockUtils.releaseLockByLua(key, value);
                log.info("Thread:{}释放锁",Thread.currentThread().getId());
            }
        }
        return null;
    }
}
