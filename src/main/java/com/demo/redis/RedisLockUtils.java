package com.demo.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author YiHaoXing
 * @description Redis锁工具类
 * 从Redis2.6.0版本开始,通过内置的Lua5.1解释器,可以使用EVAL命令对Lua脚本进行求值
 * @date 0:45 2019/6/29
 **/
@Component
@Slf4j
public class RedisLockUtils {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    /**
     * 成功获取锁返回值
     */
    private static final Long LOCK_SUCCESS = 1L;
    /**
     * 成功释放锁返回值
     */
    private static final Long UNLOCK_SUCCESS = 1L;

    /**
     * @param [lockKey, value, expireTime]
     * @return boolean
     * @author YiHaoXing
     * @description 获取锁, 原子操作
     * @date 0:45 2019/6/29
     **/
    public boolean getLock(String lockKey, String value, int expireTime) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * @param [lockKey, value]
     * @return boolean
     * @author YiHaoXing
     * @description 释放锁.非原子操作, 不推荐使用该方式释放锁
     * @date 0:44 2019/6/29
     **/
    public boolean releaseLock(String lockKey, String value) {
        if (value.equals(redisTemplate.opsForValue().get(lockKey))) {
            return redisTemplate.delete(lockKey);
        } else {
            return false;
        }
    }


    /**
     * 释放锁的LUA脚本：如果value的值与参数相等,则删除,否则返回0
     */
    public static final String UNLOCK_SCRIPT_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    /**
     * @param [lockKey, value]
     * @return boolean
     * @author YiHaoXing
     * @description 使用LUA脚本释放锁, 原子操作
     * @date 0:47 2019/6/29
     **/
    public boolean releaseLockByLua(String lockKey, String value) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(UNLOCK_SCRIPT_LUA, Long.class);
        return UNLOCK_SUCCESS.equals(redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value));
    }

    /**
     * 获取锁的LUA脚本：用setNx命令设置值,并设置过期时间
     */
    public static final String LOCK_SCRIPT_LUA = "if redis.call('setNx',KEYS[1],ARGV[1]) then if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end end";

    /**
     * @param [lockKey, value, expireTime]
     * @return boolean
     * @author YiHaoXing
     * @description 使用LUA脚本获取锁, 原子操作。过期时间单位为秒
     * @date 0:46 2019/6/29
     **/
    public boolean getLockByLua(String lockKey, String value, int expireTime) {
        RedisScript<Long> redisScript = new DefaultRedisScript<>(LOCK_SCRIPT_LUA, Long.class);
        return LOCK_SUCCESS.equals(redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value, expireTime));
    }

}
