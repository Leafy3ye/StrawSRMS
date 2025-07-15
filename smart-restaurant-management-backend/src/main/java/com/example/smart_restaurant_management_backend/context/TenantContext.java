package com.example.smart_restaurant_management_backend.context;

public class TenantContext {
    private static final ThreadLocal<Long> currentTenantId = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentStoreId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserUuid = new ThreadLocal<>();
    
    // 租户ID相关方法
    public static void setCurrentTenantId(Long tenantId) {
        currentTenantId.set(tenantId);
    }
    
    public static Long getCurrentTenantId() {
        return currentTenantId.get();
    }
    
    // 店铺ID相关方法
    public static void setCurrentStoreId(Long storeId) {
        currentStoreId.set(storeId);
    }
    
    public static Long getCurrentStoreId() {
        return currentStoreId.get();
    }
    
    // 用户UUID相关方法
    public static void setCurrentUserUuid(String userUuid) {
        currentUserUuid.set(userUuid);
    }
    
    public static String getCurrentUserUuid() {
        return currentUserUuid.get();
    }
    
    // 清理方法
    public static void clear() {
        currentTenantId.remove();
        currentStoreId.remove();
        currentUserUuid.remove();
    }
    
    // 兼容性方法（保持与现有代码的兼容）
    @Deprecated
    public static void setCurrentTenantUuid(String tenantUuid) {
        currentUserUuid.set(tenantUuid);
    }
    
    @Deprecated
    public static String getCurrentTenantUuid() {
        return currentUserUuid.get();
    }
}