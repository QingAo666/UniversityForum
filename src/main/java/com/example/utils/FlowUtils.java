package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


//针对于单次的一个频率限制
@Component
public class FlowUtils {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public boolean limitOnceCheck(String key,int blockTime){
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
            return false;
        }else {
            //如果用户没有被封禁，我们丢进去一个封禁的键（标志）
            stringRedisTemplate.opsForValue().set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }
    }

}
