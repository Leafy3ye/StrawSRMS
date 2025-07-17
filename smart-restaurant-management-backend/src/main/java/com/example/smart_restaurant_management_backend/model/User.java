package com.example.smart_restaurant_management_backend.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.smart_restaurant_management_backend.enums.UserType;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = true)  // 改为 true
    private Long tenantId;

    @Column(name = "store_id", nullable = true)  // 明确设置为 true
    private Long storeId;
    
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String uuid;
    
    @Column(nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, unique = true, length = 20)
    private String phone;
    
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType = UserType.TENANT;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "setup_completed", nullable = false)
    private Boolean setupCompleted = false;

    @Column(name = "store_mode", length = 20)
    private String storeMode; // "single" 或 "multi"

    @Column(name = "current_store_id")
    private Long currentStoreId; // 用户当前选择的店铺ID

    @Column(name = "theme_settings", columnDefinition = "TEXT")
    private String themeSettings;
    
    // 餐厅相关字段
    @Column(name = "restaurant_name")
    private String restaurantName;
    
    @Column(name = "restaurant_address")
    private String restaurantAddress;
    
    @Column(name = "restaurant_phone")
    private String restaurantPhone;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 多对一关联
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;
    
    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public Boolean getSetupCompleted() { return setupCompleted; }
    public void setSetupCompleted(Boolean setupCompleted) { this.setupCompleted = setupCompleted; }
    
    public String getThemeSettings() { return themeSettings; }
    public void setThemeSettings(String themeSettings) { this.themeSettings = themeSettings; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
    
    // 餐厅相关方法
    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    
    public String getRestaurantAddress() { return restaurantAddress; }
    public void setRestaurantAddress(String restaurantAddress) { this.restaurantAddress = restaurantAddress; }
    
    public String getRestaurantPhone() { return restaurantPhone; }
    public void setRestaurantPhone(String restaurantPhone) { this.restaurantPhone = restaurantPhone; }

    public String getStoreMode() { return storeMode; }
    public void setStoreMode(String storeMode) { this.storeMode = storeMode; }

    public Long getCurrentStoreId() { return currentStoreId; }
    public void setCurrentStoreId(Long currentStoreId) { this.currentStoreId = currentStoreId; }
}