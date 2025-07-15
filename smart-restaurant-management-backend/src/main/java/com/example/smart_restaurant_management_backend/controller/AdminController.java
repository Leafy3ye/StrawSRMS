package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.Tenant;
import com.example.smart_restaurant_management_backend.model.Store;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.repository.TenantRepository;
import com.example.smart_restaurant_management_backend.repository.StoreRepository;
import com.example.smart_restaurant_management_backend.repository.UserRepository;
import com.example.smart_restaurant_management_backend.enums.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取超级管理员看板数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData() {
        try {
            // 获取所有租户
            List<Tenant> tenants = tenantRepository.findAll();
            
            // 获取所有店铺
            List<Store> stores = storeRepository.findAll();
            
            // 获取所有用户（排除超级管理员）
            List<User> users = userRepository.findByUserType(UserType.TENANT);
            
            // 构建响应数据
            Map<String, Object> dashboardData = new HashMap<>();
            
            // 租户数据
            List<Map<String, Object>> tenantData = tenants.stream().map(tenant -> {
                Map<String, Object> tenantInfo = new HashMap<>();
                tenantInfo.put("id", tenant.getId());
                tenantInfo.put("tenantName", tenant.getTenantName());
                tenantInfo.put("email", tenant.getEmail());
                tenantInfo.put("phone", tenant.getPhone());
                tenantInfo.put("status", tenant.getStatus());
                tenantInfo.put("subscriptionPlan", tenant.getSubscriptionPlan());
                tenantInfo.put("createdAt", tenant.getCreatedAt());
                
                // 获取该租户下的店铺数量
                long storeCount = stores.stream().filter(store -> store.getTenant() != null && store.getTenant().getId().equals(tenant.getId())).count();
                tenantInfo.put("storeCount", storeCount);
                
                // 获取该租户下的用户数量
                long userCount = users.stream().filter(user -> user.getTenantId() != null && user.getTenantId().equals(tenant.getId())).count();
                tenantInfo.put("userCount", userCount);
                
                return tenantInfo;
            }).collect(Collectors.toList());
            
            // 店铺数据
            List<Map<String, Object>> storeData = stores.stream().map(store -> {
                Map<String, Object> storeInfo = new HashMap<>();
                storeInfo.put("id", store.getId());
                storeInfo.put("storeName", store.getStoreName());
                storeInfo.put("address", store.getAddress());
                storeInfo.put("phone", store.getPhone());
                storeInfo.put("status", store.getStatus());
                storeInfo.put("tenantId", store.getTenant() != null ? store.getTenant().getId() : null);
                storeInfo.put("isDefault", store.getIsDefault());
                storeInfo.put("createdAt", store.getCreatedAt());
                
                // 获取租户名称
                if (store.getTenant() != null) {
                    tenants.stream()
                        .filter(tenant -> tenant.getId().equals(store.getTenant().getId()))
                        .findFirst()
                        .ifPresent(tenant -> storeInfo.put("tenantName", tenant.getTenantName()));
                }
                
                return storeInfo;
            }).collect(Collectors.toList());
            
            // 统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalTenants", tenants.size());
            statistics.put("totalStores", stores.size());
            statistics.put("totalUsers", users.size());
            statistics.put("activeTenants", tenants.stream().filter(t -> "ACTIVE".equals(t.getStatus().toString())).count());
            statistics.put("activeStores", stores.stream().filter(s -> "ACTIVE".equals(s.getStatus().toString())).count());
            
            dashboardData.put("tenants", tenantData);
            dashboardData.put("stores", storeData);
            dashboardData.put("statistics", statistics);
            
            return ResponseEntity.ok(dashboardData);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取看板数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取租户详细信息
     */
    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<?> getTenantDetail(@PathVariable Long tenantId) {
        try {
            Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
            if (tenant == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取该租户下的所有店铺
            List<Store> stores = storeRepository.findByTenant_Id(tenantId);
            
            // 获取该租户下的所有用户
            List<User> users = userRepository.findByTenantId(tenantId);
            
            Map<String, Object> tenantDetail = new HashMap<>();
            tenantDetail.put("tenant", tenant);
            tenantDetail.put("stores", stores);
            tenantDetail.put("users", users.stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("phone", user.getPhone());
                userInfo.put("createdAt", user.getCreatedAt());
                userInfo.put("setupCompleted", user.getSetupCompleted());
                return userInfo;
            }).collect(Collectors.toList()));
            
            return ResponseEntity.ok(tenantDetail);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取租户详情失败: " + e.getMessage());
        }
    }
}