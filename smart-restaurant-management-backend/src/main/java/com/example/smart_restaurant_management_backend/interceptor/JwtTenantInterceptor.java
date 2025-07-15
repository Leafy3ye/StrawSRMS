package com.example.smart_restaurant_management_backend.interceptor;

import com.example.smart_restaurant_management_backend.dto.UserDTO;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.service.UserService;
import com.example.smart_restaurant_management_backend.service.StoreService;
import com.example.smart_restaurant_management_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtTenantInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    // 延迟获取 UserService 和 StoreService
    private UserService getUserService() {
        return applicationContext.getBean(UserService.class);
    }
    
    private StoreService getStoreService() {
        return applicationContext.getBean(StoreService.class);
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.isTokenValid(token)) {
                String userUuid = jwtUtil.extractUsername(token);
                
                // 根据用户UUID获取租户信息
                UserDTO user = getUserService().getUserByUuid(userUuid);
                if (user != null) {
                    // 设置租户上下文
                    TenantContext.setCurrentTenantId(user.getTenantId());
                    TenantContext.setCurrentUserUuid(userUuid);
                    
                    // 获取用户的默认店铺或从请求头中获取店铺ID
                    String storeIdHeader = request.getHeader("X-Store-Id");
                    Long storeId = null;
                    
                    if (storeIdHeader != null) {
                        try {
                            storeId = Long.parseLong(storeIdHeader);
                            // 验证店铺是否属于当前租户
                            if (!getStoreService().isStoreOwnedByTenant(storeId, user.getTenantId())) {
                                storeId = null;
                            }
                        } catch (NumberFormatException e) {
                            storeId = null;
                        }
                    }
                    
                    if (storeId == null) {
                        // 获取租户的默认店铺
                        storeId = getStoreService().getDefaultStoreIdByTenantId(user.getTenantId());
                    }
                    
                    if (storeId != null) {
                        TenantContext.setCurrentStoreId(storeId);
                    }
                    
                    // 设置请求属性
                    request.setAttribute("currentUser", userUuid);
                    request.setAttribute("currentTenantId", user.getTenantId());
                    request.setAttribute("currentStoreId", storeId);
                    
                    return true;
                }
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