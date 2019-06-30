package com.test.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.test.demo.DemoApplication
 * @description 启动类
 * @date 2019/6/15 10:05
 */
@SpringBootApplication
public class RedisLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisLockApplication.class);
    }

}
