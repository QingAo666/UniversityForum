package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    //使token失效
    public boolean inValidateJwt(String headerToken){
        String token = this.convertToken(headerToken);
        if (token == null) return false;
        //验证令牌
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();//创建验证对象
        try{
            DecodedJWT verify = jwtVerifier.verify(token);//生成jwt对象
            String id = verify.getId();//取出独有的uuid
            return deletedToken(id,verify.getExpiresAt());
        }catch (JWTVerificationException e){
            return false;
        }
    }

    //删除Token
    private boolean deletedToken(String uuid,Date time){
        //判断是否已经失效，失效后就不用继续删除加入黑名单了
        if(this.isInvalidToken(uuid))
            return false;
        //判断令牌时间是否过期
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(),0);
        //存进黑名单
        template.opsForValue().set(Const.JWT_BLACK_LIST+uuid,"",expire, TimeUnit.MILLISECONDS);
        return true;
    }

    //判断当前Token是否失效(是否在黑名单中)
    private boolean isInvalidToken(String uuid){
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }




    public String createJwt(UserDetails details,int id,String username){
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expired = this.expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())//每个jwt都携带一个独有的id
                .withClaim("id",id)
                .withClaim("name",username)
                .withClaim("authorities",details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expired)//过期时间
                .withIssuedAt(new Date())//签发时间
                .sign(algorithm);
    }

    //解析token
    public DecodedJWT resolveJwt(String headerToken){
        //判断token是否存在 以及去除Bearer
        String token = this.convertToken(headerToken);
        if(token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();//创建验证对象
        try{
            DecodedJWT verify = jwtVerifier.verify(token);//验证Token对象
            //验证是否失效
            if(this.isInvalidToken(verify.getId()))
                return null;
            //验证令牌是否过期
            Date expiresAt = verify.getExpiresAt();//获取令牌过期时间
            return new Date().after(expiresAt) ? null : verify;
        }catch (JWTVerificationException e){
            return null;//验证失败返回空
        }
    }

    //判断token是否存在
    public String convertToken(String headerToken){
        if(headerToken==null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

    //解析jwt中的用户信息
    public UserDetails toUser(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer toInt(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    //计算过期时间
    public Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR,expire*24);
        return calendar.getTime();
    }
}
