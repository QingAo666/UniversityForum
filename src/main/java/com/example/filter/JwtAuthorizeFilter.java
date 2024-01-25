package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {
    @Resource
    JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //一般是在这个请求头里携带我们的token
        String authorization = request.getHeader("Authorization");
        //Token解析成jwt
        DecodedJWT jwt = jwtUtils.resolveJwt(authorization);
        if(jwt != null){
            UserDetails user = jwtUtils.toUser(jwt);
            //生成验证信息 （封装用户名密码认证信息的一个类，）
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //SecurityContextHolder用于管理当前用户的安全上下文信息，包括认证信息、授权信息等。
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            //业务常用
            request.setAttribute("id",jwtUtils.toInt(jwt));
        }
        filterChain.doFilter(request,response);
    }
}
