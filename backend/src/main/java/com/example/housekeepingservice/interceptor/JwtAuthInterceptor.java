package com.example.housekeepingservice.interceptor;

import com.example.housekeepingservice.common.Result;
import com.example.housekeepingservice.common.ResultCode;
import com.example.housekeepingservice.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    @Value("${jwt.header}")
    private String tokenHeader;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(tokenHeader);
        if (token == null || !jwtUtil.isTokenValid(token)) {
            // 1. 消除泛型：直接用非泛型Result
            response.setContentType("application/json;charset=UTF-8");
            Result result = Result.error(ResultCode.NOT_LOGIN.getCode(), ResultCode.NOT_LOGIN.getMessage());
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return false;
        }

        // 2. 显式声明Claims类型（避免var关键字的适配问题）
        Claims claims = jwtUtil.parseToken(token);
        request.setAttribute("userId", claims.get("userId"));
        request.setAttribute("role", claims.get("role"));
        return true;
    }
}