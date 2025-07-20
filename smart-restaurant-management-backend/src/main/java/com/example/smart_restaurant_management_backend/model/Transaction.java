package com.example.smart_restaurant_management_backend.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 租户ID - 改为Long类型
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    // 新增店铺ID字段
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "table_name", length = 50)
    private String tableName;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 会员结算相关字段
    @Column(name = "payment_type", length = 20)
    private String paymentType = "cash"; // cash: 正常结算, member: 会员结算

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "member_name", length = 50)
    private String memberName;

    @Column(name = "member_phone", length = 20)
    private String memberPhone;

    @Column(name = "member_level", length = 50)
    private String memberLevel;

    @Column(name = "original_amount", precision = 10, scale = 2)
    private BigDecimal originalAmount; // 原始金额（折扣前）

    @Column(name = "discount_rate", precision = 3, scale = 2)
    private BigDecimal discountRate; // 折扣率

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount; // 折扣金额

    @Column(name = "actual_amount", precision = 10, scale = 2)
    private BigDecimal actualAmount; // 实际收款金额（折扣后）
    
    // JPA关联关系
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
        createdAt = LocalDateTime.now();
    }

    // 构造函数
    public Transaction() {}

    public Transaction(Long tenantId, Long storeId, Long tableId, String tableName, BigDecimal totalAmount, Integer orderCount) {
        this.tenantId = tenantId;
        this.storeId = storeId;
        this.tableId = tableId;
        this.tableName = tableName;
        this.totalAmount = totalAmount;
        this.orderCount = orderCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    // 会员结算相关字段的getter和setter
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }

    public String getMemberLevel() { return memberLevel; }
    public void setMemberLevel(String memberLevel) { this.memberLevel = memberLevel; }

    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }

    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
}