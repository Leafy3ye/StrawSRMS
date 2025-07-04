package com.example.smart_restaurant_management_backend.interceptor;

import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtTenantInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.isTokenValid(token)) {
                String tenantId = jwtUtil.extractTenantId(token);
                String username = jwtUtil.extractUsername(token);
                
                // 设置租户上下文
                TenantContext.setCurrentTenantUuid(tenantId);
                
                // 可选：设置用户上下文
                request.setAttribute("currentUser", username);
                request.setAttribute("currentTenantId", tenantId);
                
                return true;
            }
        }
        
        // Token 无效或不存在
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"未授权访问\",\"code\":401}");
        return false;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
    }
}