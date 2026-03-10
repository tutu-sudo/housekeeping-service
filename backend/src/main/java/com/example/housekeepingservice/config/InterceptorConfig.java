package com.example.housekeepingservice.config;

import com.example.housekeepingservice.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器：指定哪些接口需要验证Token，哪些不用
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                // 需要登录的接口（拦截）
                .addPathPatterns("/api/customer/**")
                // 不需要登录的接口（放行：注册、登录）
                .excludePathPatterns("/api/auth/register")
                .excludePathPatterns("/api/auth/login");
    }
}