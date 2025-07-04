package com.example.smart_restaurant_management_backend.context;

public class TenantContext {
    private static final ThreadLocal<String> currentTenantUuid = new ThreadLocal<>();
    
    public static void setCurrentTenantUuid(String tenantUuid) {
        currentTenantUuid.set(tenantUuid);
    }
    
    public static String getCurrentTenantUuid() {
        return currentTenantUuid.get();
    }
    
    public static void clear() {
        currentTenantUuid.remove();
    }
}