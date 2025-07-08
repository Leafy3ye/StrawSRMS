package com.example.smart_restaurant_management_backend.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_levels")
public class MemberLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 租户ID，确保数据隔离
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    // 等级名称
    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;

    // 折扣率（0.1-1.0，如0.9表示9折）
    @Column(name = "discount_rate", nullable = false, precision = 3, scale = 2)
    private BigDecimal discountRate;

    // 积分倍率
    @Column(name = "point_rate", nullable = false)
    private Integer pointRate;

    // 会员权益描述
    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    // 升级条件
    @Column(name = "upgrade_condition", length = 500)
    private String upgradeCondition;

    // 排序权重（数字越小优先级越高）
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // 是否启用
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
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

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }

    public Integer getPointRate() { return pointRate; }
    public void setPointRate(Integer pointRate) { this.pointRate = pointRate; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getUpgradeCondition() { return upgradeCondition; }
    public void setUpgradeCondition(String upgradeCondition) { this.upgradeCondition = upgradeCondition; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}