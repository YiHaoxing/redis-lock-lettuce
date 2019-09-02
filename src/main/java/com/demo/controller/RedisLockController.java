package com.demo.controller;

import com.demo.annotation.RedisLock;
import com.demo.redis.RedisLockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.test.demo.controller.RedisLockController
 * @description
 * @date 2019/6/28 23:47
 */
@RestController
public class RedisLockController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedisLockUtils redisLockUtils;

    /**
     * @return java.lang.String
     * @author YiHaoXing
     * @description 通过AOP加锁/释放锁
     * @date 23:55 2019/6/28
     **/
    @GetMapping("/A")
    @RedisLock(key = "A", value = "aaa", expireTime = 30)
    public String testRedisLock1() {
        //获取到锁后才能走到这里执行业务逻辑
        System.out.println(redisTemplate.opsForValue().get("A"));
        //do something.
        return "Hello World";
    }

    /**
     * @param []
     * @return java.lang.String
     * @author YiHaoXing
     * @description 通过代码加锁/释放锁
     * @date 16:54 2019/6/30
     **/
    @GetMapping("/B")
    public String testRedisLock2() {
        if (redisLockUtils.getLock("B", "bbb", 30)) {
            try {
                //获取到锁后执行业务流程
                System.out.println(redisTemplate.opsForValue().get("B"));
            } catch (Exception e) {
                //do something.
            } finally {
                //释放锁
                boolean result = redisLockUtils.releaseLock("B", "bbb");
                System.out.println(result);
            }
        }
        return "Hello World";
    }

    /**
     * @author YiHaoXing
     * @description 通过LUA脚本加锁/释放锁.保证原子性
     * @date 2019/9/2 0:19
     * @param []
     * @return java.lang.String
     **/
    @GetMapping("/C")
    public String testRedisLock3() {
        boolean lock = redisLockUtils.getLockByLua("C", "ccc", 30);
        if (lock) {
            try {
                //获取到锁后执行业务流程
                System.out.println(redisTemplate.opsForValue().get("C"));
            } catch (Exception e) {
                //do something.
            } finally {
                //释放锁
                boolean result = redisLockUtils.releaseLockByLua("C", "ccc");
                System.out.println(result);
            }
        }
        return "Hello World";
    }

}
