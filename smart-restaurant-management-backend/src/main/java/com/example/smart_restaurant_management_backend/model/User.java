package com.example.smart_restaurant_management_backend.model;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 添加租户ID字段 - 用于多租户隔离
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    // 添加UUID字段 - 用于商业化多租户系统的用户唯一标识
    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // 新增：邮箱字段
    @Column(nullable = false, unique = true)
    private String email;

    // 新增：手机号字段
    @Column(nullable = false, unique = true)
    private String phone;

    // 新增：邮箱验证状态
    @Column(nullable = false)
    private Boolean emailVerified = false;

    private String avatarUrl;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = java.util.UUID.randomUUID().toString();
        }
        if (tenantId == null) {
            // 如果没有设置租户ID，使用UUID作为默认租户ID
            tenantId = uuid;
        }
        createdAt = new Date();  // 修改：使用Date而不是LocalDateTime
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // UUID getter and setter
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 新增字段的getter和setter
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
    
    // 添加用户类型枚举
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType = UserType.TENANT;
    
    // 餐厅信息
    @Column(name = "restaurant_name")
    private String restaurantName;
    
    @Column(name = "restaurant_address")
    private String restaurantAddress;
    
    @Column(name = "restaurant_phone")
    private String restaurantPhone;
    
    // 设置完成标志
    @Column(name = "setup_completed")
    private Boolean setupCompleted = false;
    
    // 主题设置
    @Column(name = "theme_settings", columnDefinition = "TEXT")
    private String themeSettings;

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    // 在 User.java 的最后添加缺失的 getter/setter 方法
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
    public String getRestaurantAddress() {
        return restaurantAddress;
    }
    
    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }
    
    public String getRestaurantPhone() {
        return restaurantPhone;
    }
    
    public void setRestaurantPhone(String restaurantPhone) {
        this.restaurantPhone = restaurantPhone;
    }
    
    public Boolean getSetupCompleted() {
        return setupCompleted;
    }
    
    public void setSetupCompleted(Boolean setupCompleted) {
        this.setupCompleted = setupCompleted;
    }
    
    // 添加租户ID的getter和setter
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    // 主题设置的getter和setter
    public String getThemeSettings() {
        return themeSettings;
    }

    public void setThemeSettings(String themeSettings) {
        this.themeSettings = themeSettings;
    }
}