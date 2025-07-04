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
                // 在excludePathPatterns中添加以下路径
                .excludePathPatterns(
                    "/api/users/login",
                    "/api/users/register", 
                    "/api/users/captcha",
                    "/api/users/send-email-code",
                    "/api/users/verify-captcha",           // 新增
                    "/api/users/send-reset-password-code", // 新增
                    "/api/users/reset-password"            // 新增
                );  // 排除登录、注册、验证码、找回密码相关接口
    }
}
