package com.example.filter;

import com.example.entity.RestBean;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(Const.ORDER_LIMIT)
public class  FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template;

    String i;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();

        if(this.tryCount(address)){
            chain.doFilter(request,response);
        }else{
            this.writeBlockMessage(response);
        }
    }

    private void writeBlockMessage(HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden("操作频繁,请稍后再试").asJsonString());
        log.info("IP请求频率过高，被拦截,redis中的key:"+Const.Flow_LIMIT_BLOCK+i);

    }

    private boolean tryCount(String ip){
        i = ip;
        synchronized (ip.intern()){
            if(Boolean.TRUE.equals(template.hasKey(Const.Flow_LIMIT_BLOCK+ip)))
                return false;
           return this.limitPeriodCheck(ip);
        }
    }

    private boolean limitPeriodCheck(String ip){
        if(Boolean.TRUE.equals(template.hasKey(Const.Flow_LIMIT_COUNTER + ip))){
            long increment = Optional.ofNullable(template.opsForValue().increment(Const.Flow_LIMIT_COUNTER+ip)).orElse(0L);
            if(increment > 50){
                template.opsForValue().set(Const.Flow_LIMIT_BLOCK+ip,"",30,TimeUnit.SECONDS);
                return false;
            }
        }else{
            template.opsForValue().set(Const.Flow_LIMIT_COUNTER+ip,"1",3,TimeUnit.SECONDS);
        }
        return true;
    }



}
