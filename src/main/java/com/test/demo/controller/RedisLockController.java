package com.test.demo.controller;

import com.test.demo.annotation.RedisLock;
import com.test.demo.redis.RedisLockUtils;
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
     * @description 通过注解加锁
     * @date 23:55 2019/6/28
     **/
    @GetMapping("/A")
    @RedisLock(key = "D", value = "VALUE-D", expireTime = 30000)
    public String testRedisLock1() {
        System.out.println(redisTemplate.opsForValue().get("D"));
        return "Hello World";
    }

    @GetMapping("/B")
    public String testRedisLock2() {

        if(redisLockUtils.getLock("F", "VALUE-F",10000)){
            try {
                System.out.println(redisTemplate.opsForValue().get("F"));
            } catch (Exception e) {
                //TODO
            } finally {
                //释放锁
                redisLockUtils.releaseLock("F","VALUE-F");
            }
        }
        return "Hello World";
    }

}
