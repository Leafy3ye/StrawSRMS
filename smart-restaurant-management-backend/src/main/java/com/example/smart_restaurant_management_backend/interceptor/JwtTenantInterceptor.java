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
                    // 检查是否为超级管理员
                    if ("SUPER_ADMIN".equals(user.getUserType())) {
                        // 超级管理员可以通过请求头指定要查看的租户和店铺
                        String tenantIdHeader = request.getHeader("X-Tenant-ID");
                        String storeIdHeader = request.getHeader("X-Store-Id");

                        if (tenantIdHeader != null) {
                            try {
                                Long tenantId = Long.parseLong(tenantIdHeader);
                                TenantContext.setCurrentTenantId(tenantId);

                                if (storeIdHeader != null) {
                                    try {
                                        Long storeId = Long.parseLong(storeIdHeader);
                                        TenantContext.setCurrentStoreId(storeId);
                                    } catch (NumberFormatException e) {
                                        // 忽略无效的店铺ID
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // 忽略无效的租户ID
                            }
                        }

                        TenantContext.setCurrentUserUuid(userUuid);
                        TenantContext.setIsSuperAdmin(true);
                        request.setAttribute("currentUser", userUuid);
                        request.setAttribute("userType", "SUPER_ADMIN");
                        return true;
                    }
                    
                    // 普通租户用户的处理逻辑
                    TenantContext.setCurrentTenantId(user.getTenantId());
                    TenantContext.setCurrentUserUuid(userUuid);
                    
                    // 获取用户的当前店铺ID，优先级：用户设置的currentStoreId > 请求头 > 默认店铺
                    Long storeId = user.getCurrentStoreId();

                    // 如果用户没有设置当前店铺，尝试从请求头获取
                    if (storeId == null) {
                        String storeIdHeader = request.getHeader("X-Store-Id");
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
                    }

                    // 如果还是没有店铺ID，获取租户的默认店铺
                    if (storeId == null) {
                        storeId = getStoreService().getDefaultStoreIdByTenantId(user.getTenantId());
                    }

                    // 验证用户设置的currentStoreId是否有效
                    if (storeId != null && user.getCurrentStoreId() != null) {
                        if (!getStoreService().isStoreOwnedByTenant(storeId, user.getTenantId())) {
                            // 如果用户设置的店铺无效，重置为默认店铺
                            storeId = getStoreService().getDefaultStoreIdByTenantId(user.getTenantId());
                        }
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