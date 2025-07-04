package com.example.smart_restaurant_management_backend.config;

import com.example.smart_restaurant_management_backend.interceptor.JwtTenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTenantInterceptor jwtTenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTenantInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns("/api/users/login", "/api/users/register");  // 排除登录和注册接口
    }
}
