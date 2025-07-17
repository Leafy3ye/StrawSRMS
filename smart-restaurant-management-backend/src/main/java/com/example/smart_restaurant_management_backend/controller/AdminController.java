package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.Tenant;
import com.example.smart_restaurant_management_backend.model.Store;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.model.Member;
import com.example.smart_restaurant_management_backend.model.Order;
import com.example.smart_restaurant_management_backend.model.Dish;
import com.example.smart_restaurant_management_backend.repository.TenantRepository;
import com.example.smart_restaurant_management_backend.repository.StoreRepository;
import com.example.smart_restaurant_management_backend.repository.UserRepository;
import com.example.smart_restaurant_management_backend.enums.UserType;
import com.example.smart_restaurant_management_backend.service.MemberService;
import com.example.smart_restaurant_management_backend.service.OrderService;
import com.example.smart_restaurant_management_backend.service.DishService;
import com.example.smart_restaurant_management_backend.service.UserService;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
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

    @Autowired
    private MemberService memberService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DishService dishService;

    @Autowired
    private UserService userService;

    /**
     * 获取超级管理员看板数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData() {
        try {
            // 获取所有租户（排除系统租户）
            List<Tenant> allTenants = tenantRepository.findAll();
            List<Tenant> tenants = allTenants.stream()
                .filter(tenant -> !"SYSTEM".equals(tenant.getSubscriptionPlan()) &&
                                !"系统租户".equals(tenant.getTenantName()))
                .collect(Collectors.toList());

            // 获取所有店铺（排除系统店铺）
            List<Store> allStores = storeRepository.findAll();
            List<Store> stores = allStores.stream()
                .filter(store -> {
                    // 排除系统默认店铺
                    if ("系统默认店铺".equals(store.getStoreName())) {
                        return false;
                    }
                    // 排除属于系统租户的店铺
                    if (store.getTenant() != null &&
                        ("SYSTEM".equals(store.getTenant().getSubscriptionPlan()) ||
                         "系统租户".equals(store.getTenant().getTenantName()))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

            // 获取所有用户（排除超级管理员和系统用户）
            List<User> allUsers = userRepository.findByUserType(UserType.TENANT);
            List<User> users = allUsers.stream()
                .filter(user -> {
                    // 排除admin用户
                    if ("admin".equals(user.getUsername())) {
                        return false;
                    }
                    // 排除属于系统租户的用户
                    if (user.getTenantId() != null) {
                        Optional<Tenant> userTenant = tenantRepository.findById(user.getTenantId());
                        if (userTenant.isPresent() &&
                            ("SYSTEM".equals(userTenant.get().getSubscriptionPlan()) ||
                             "系统租户".equals(userTenant.get().getTenantName()))) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
            
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
            
            // 手动构建租户信息，避免循环引用
            Map<String, Object> tenantInfo = new HashMap<>();
            tenantInfo.put("id", tenant.getId());
            tenantInfo.put("tenantName", tenant.getTenantName());
            tenantInfo.put("email", tenant.getEmail());
            tenantInfo.put("phone", tenant.getPhone());
            tenantInfo.put("status", tenant.getStatus());
            tenantInfo.put("subscriptionPlan", tenant.getSubscriptionPlan());
            tenantInfo.put("createdAt", tenant.getCreatedAt());
            tenantInfo.put("updatedAt", tenant.getUpdatedAt());

            // 手动构建店铺信息，避免循环引用
            List<Map<String, Object>> storeInfos = stores.stream().map(store -> {
                Map<String, Object> storeInfo = new HashMap<>();
                storeInfo.put("id", store.getId());
                storeInfo.put("storeName", store.getStoreName());
                storeInfo.put("address", store.getAddress());
                storeInfo.put("phone", store.getPhone());
                storeInfo.put("status", store.getStatus());
                storeInfo.put("isDefault", store.getIsDefault());
                storeInfo.put("createdAt", store.getCreatedAt());
                storeInfo.put("updatedAt", store.getUpdatedAt());
                return storeInfo;
            }).collect(Collectors.toList());

            // 手动构建用户信息，避免循环引用
            List<Map<String, Object>> userInfos = users.stream().map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("phone", user.getPhone());
                userInfo.put("createdAt", user.getCreatedAt());
                userInfo.put("setupCompleted", user.getSetupCompleted());
                userInfo.put("restaurantName", user.getRestaurantName());
                return userInfo;
            }).collect(Collectors.toList());

            Map<String, Object> tenantDetail = new HashMap<>();
            tenantDetail.put("tenant", tenantInfo);
            tenantDetail.put("stores", storeInfos);
            tenantDetail.put("users", userInfos);
            
            return ResponseEntity.ok(tenantDetail);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取租户详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定租户的会员数据
     */
    @GetMapping("/tenants/{tenantId}/members")
    public ResponseEntity<?> getTenantMembers(@PathVariable Long tenantId,
                                            @RequestParam(required = false) Long storeId) {
        try {
            // 检查当前用户是否为超级管理员
            Boolean isSuperAdmin = TenantContext.getIsSuperAdmin();
            if (!Boolean.TRUE.equals(isSuperAdmin)) {
                return ResponseEntity.status(403).body("权限不足");
            }

            // 保存当前上下文
            Long originalTenantId = TenantContext.getCurrentTenantId();
            Long originalStoreId = TenantContext.getCurrentStoreId();

            // 设置指定的租户上下文
            TenantContext.setCurrentTenantId(tenantId);
            if (storeId != null) {
                TenantContext.setCurrentStoreId(storeId);
            }

            List<Member> members = memberService.getAllMembers();

            // 恢复原始上下文
            TenantContext.setCurrentTenantId(originalTenantId);
            TenantContext.setCurrentStoreId(originalStoreId);

            return ResponseEntity.ok(members);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取租户会员数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定租户的订单数据
     */
    @GetMapping("/tenants/{tenantId}/orders")
    public ResponseEntity<?> getTenantOrders(@PathVariable Long tenantId,
                                           @RequestParam(required = false) Long storeId) {
        try {
            // 检查当前用户是否为超级管理员
            Boolean isSuperAdmin = TenantContext.getIsSuperAdmin();
            if (!Boolean.TRUE.equals(isSuperAdmin)) {
                return ResponseEntity.status(403).body("权限不足");
            }

            // 保存当前上下文
            Long originalTenantId = TenantContext.getCurrentTenantId();
            Long originalStoreId = TenantContext.getCurrentStoreId();

            // 设置指定的租户上下文
            TenantContext.setCurrentTenantId(tenantId);
            if (storeId != null) {
                TenantContext.setCurrentStoreId(storeId);
            }

            List<Order> orders = orderService.findAll();

            // 恢复原始上下文
            TenantContext.setCurrentTenantId(originalTenantId);
            TenantContext.setCurrentStoreId(originalStoreId);

            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取租户订单数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定租户的菜品数据
     */
    @GetMapping("/tenants/{tenantId}/dishes")
    public ResponseEntity<?> getTenantDishes(@PathVariable Long tenantId,
                                           @RequestParam(required = false) Long storeId) {
        try {
            // 检查当前用户是否为超级管理员
            Boolean isSuperAdmin = TenantContext.getIsSuperAdmin();
            if (!Boolean.TRUE.equals(isSuperAdmin)) {
                return ResponseEntity.status(403).body("权限不足");
            }

            // 保存当前上下文
            Long originalTenantId = TenantContext.getCurrentTenantId();
            Long originalStoreId = TenantContext.getCurrentStoreId();

            // 设置指定的租户上下文
            TenantContext.setCurrentTenantId(tenantId);
            if (storeId != null) {
                TenantContext.setCurrentStoreId(storeId);
            }

            List<Dish> dishes = dishService.getAllDishes();

            // 恢复原始上下文
            TenantContext.setCurrentTenantId(originalTenantId);
            TenantContext.setCurrentStoreId(originalStoreId);

            return ResponseEntity.ok(dishes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取租户菜品数据失败: " + e.getMessage());
        }
    }

    /**
     * 删除租户及其所有相关数据
     */
    @DeleteMapping("/tenants/{tenantId}")
    public ResponseEntity<?> deleteTenant(@PathVariable Long tenantId) {
        try {
            // 检查当前用户是否为超级管理员
            Boolean isSuperAdmin = TenantContext.getIsSuperAdmin();
            if (!Boolean.TRUE.equals(isSuperAdmin)) {
                return ResponseEntity.status(403).body("权限不足");
            }

            // 检查租户是否存在
            Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
            if (!tenantOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Tenant tenant = tenantOpt.get();

            // 防止删除系统租户
            if ("SYSTEM".equals(tenant.getSubscriptionPlan()) || "系统租户".equals(tenant.getTenantName())) {
                return ResponseEntity.badRequest().body("不能删除系统租户");
            }

            // 调用 UserService 的删除账户方法来删除所有相关数据
            // 这里我们需要创建一个管理员专用的删除方法
            userService.deleteTenantByAdmin(tenantId);

            return ResponseEntity.ok("租户删除成功");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除租户失败: " + e.getMessage());
        }
    }
}