package com.example.smart_restaurant_management_backend.dto;

public class UserDTO {
    private Long id;
    private Long tenantId;
    private Long storeId;
    private String uuid;
    private String username;
    private String email;
    private String phone;
    private Boolean emailVerified;
    private String avatarUrl;
    private String restaurantName;
    private Boolean setupCompleted;
    private String themeSettings;
    private String userType; // 添加用户类型字段

    // 不包含密码，用于返回给前端
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public Boolean getSetupCompleted() {
        return setupCompleted;
    }
    
    public void setSetupCompleted(Boolean setupCompleted) {
        this.setupCompleted = setupCompleted;
    }
    
    public String getThemeSettings() {
        return themeSettings;
    }
    
    public void setThemeSettings(String themeSettings) {
        this.themeSettings = themeSettings;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
}