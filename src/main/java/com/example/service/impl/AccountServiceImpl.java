package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import io.netty.util.Timeout;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    FlowUtils flowUtils;
    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    //当有用户登陆时，spring security会调用loadUserByUsername方法，并把用户输入的账号传进来,但是并不传密码，
    // 因为这个方法不会做用户名和密码的校验，该方法只是根据用户名从数据库中查出来用户的信息，
    // 然后将其交给spring security来根据这个信息和用户输入的账号密码来校验登陆是否成功。
    // 如果登陆成功，那么spring security会将用户信息、权限等保存在内存中，以便后边使用。
    // 也就是说,登陆校验是由spring security来做的，不需要我们显式的处理
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username",text)
                .or()
                .eq("email",text)
                .one();
    }

    @Override
    public String registerEmailVerifyCode(String type,String email,String ip) {
        //防止一个用户同时请求两百次
        synchronized (ip.intern()){
            if(!this.verifyLimit(ip)){
                return "请求频繁，请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(899999)+100000;
            Map<String,Object> data = Map.of("type",type,"email",email,"code",code);
            amqpTemplate.convertAndSend("mail",data);
            stringRedisTemplate.opsForValue().set(Const.VERIFY_EMAIL_DATA+email,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }
    }

    //是否在限制时间内
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT+ip;
        return flowUtils.limitOnceCheck(key,60);
    }

}
