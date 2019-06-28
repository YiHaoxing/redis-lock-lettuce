package com.test.demo.redis;

import lombok.Lombok;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author YiHaoXing
 * @description Redis锁工具类
 * @date 0:45 2019/6/29
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
@Component
public class RedisLockUtils {

    //直接通过redisTemplate来操作Redis，至于底层是采用Jedis还是Lettuce不需要关注。

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String LOCK_SUCCESS = "OK";

    private static final Long RELEASE_LOCK_SUCCESS=1L;

    //从 Redis 2.6.0 版本开始，通过内置的 Lua 5.1解释器，可以使用 EVAL 命令对 Lua 脚本进行求值。
    //如果使用LUA脚本,服务器上需要安装LUA运行环境.



    /**
     * @param [lockKey, value, expireTime]
     * @return boolean
     * @author YiHaoXing
     * @description 获取锁, 原子操作
     * @date 0:45 2019/6/29
     **/
    public boolean getLock(String lockKey, String value, int expireTime) {
        return redisTemplate.opsForValue().setIfAbsent(lockKey, value, expireTime, TimeUnit.MILLISECONDS);
    }

    /**
     * @param [lockKey, value]
     * @return boolean
     * @author YiHaoXing
     * @description 非原子操作, 不推荐使用该方式释放锁
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
    public static final String RELEASE_SCRIPT_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    /**
     * @param [lockKey, value]
     * @return boolean
     * @author YiHaoXing
     * @description 使用LUA脚本释放锁, 原子操作
     * @date 0:47 2019/6/29
     **/
    public boolean releaseLockByLua(String lockKey, String value) {
        //小细节 Long.class是指LUA脚本执行后的返回值类型。但是Redis中，这个类型只有Long，List，Boolean。所以用String或者Integer接受时就会报错。
        //原因 https://segmentfault.com/q/1010000015092788/a-1020000015558682

        //org.springframework.data.redis.connection.ReturnType

        ///**
        //	 * @param javaType can be {@literal null} which translates to {@link ReturnType#STATUS}.
        //	 * @return never {@literal null}.
        //	 */
        //	public static ReturnType fromJavaType(@Nullable Class<?> javaType) {
        //
        //		if (javaType == null) {
        //			return ReturnType.STATUS;
        //		}
        //		if (javaType.isAssignableFrom(List.class)) {
        //			return ReturnType.MULTI;
        //		}
        //		if (javaType.isAssignableFrom(Boolean.class)) {
        //			return ReturnType.BOOLEAN;
        //		}
        //		if (javaType.isAssignableFrom(Long.class)) {
        //			return ReturnType.INTEGER;
        //		}
        //		return ReturnType.VALUE;
        //	}
        RedisScript<Long> redisScript = new DefaultRedisScript<>(RELEASE_SCRIPT_LUA, Long.class);
        Long execute = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value);
        return RELEASE_LOCK_SUCCESS.equals(execute);
    }

    /**
     * 获取锁的LUA脚本：用setNx命令设置值,并设置过期时间
     */
    public static final String LOCK_SCRIPT_LUA = "if redis.call('setNx',KEYS[1],ARGV[1]) then if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end end";

    /**
     * @param [lockKey, value, expireTime]
     * @return boolean
     * @author YiHaoXing
     * @description 使用LUA脚本获取锁, 原子操作
     * @date 0:46 2019/6/29
     **/
    public boolean getLockByLua(String lockKey, String value, int expireTime) {
        RedisScript<String> redisScript = new DefaultRedisScript<>(LOCK_SCRIPT_LUA, String.class);
        return LOCK_SUCCESS.equals(redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value, expireTime));
    }


}
