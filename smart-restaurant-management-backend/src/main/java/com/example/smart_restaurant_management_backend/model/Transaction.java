package com.example.smart_restaurant_management_backend.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 添加租户ID字段
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "table_id", nullable = false)
    private Integer tableId;

    @Column(name = "table_name", length = 50)
    private String tableName;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private Double totalAmount;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 构造函数
    public Transaction() {}

    // 修改构造函数
    public Transaction(String tenantId, Integer tableId, String tableName, Double totalAmount, Integer orderCount) {
        this.tenantId = tenantId;
        this.tableId = tableId;
        this.tableName = tableName;
        this.totalAmount = totalAmount;
        this.orderCount = orderCount;
    }

    // 添加租户ID的getter和setter
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}