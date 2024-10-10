package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
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

    /**针对于在时间段内多次请求限制，如3秒内限制请求20次，超出频率则封禁一段时间
    * @param counterKey 计数键
    * @param frequency 请求频率
    * @param period 计数周期
    * @return 是否通过限流检査
    */
    public boolean limitPeriodCounterCheck(String counterKey,int frequency, int period){
        return this.internalCheck(counterKey, frequency, period,overclock -> !overclock);
    }

    /**
    /*内部使用请求限制主要逻辑
    *@param key 计数键
    *@param frequency 请求频率
    *@param period 计数周期
    *@param action 限制行为与策略
    *@return 是否通过限流检査
    */

    private boolean internalCheck(String key, int frequency, int period, LimitAction action) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            Long value = Optional.ofNullable(stringRedisTemplate.opsForValue().increment(key)).orElse(0L);
            return action.run(value > frequency);
        } else {
            stringRedisTemplate.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;
        }
    }

    private interface LimitAction{
        boolean run(boolean overclock);
    }
}
