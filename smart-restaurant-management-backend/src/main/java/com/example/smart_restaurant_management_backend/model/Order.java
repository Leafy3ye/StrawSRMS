package com.example.smart_restaurant_management_backend.model;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 添加租户ID字段
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "table_id", nullable = false)
    private Integer tableId;

    @Column(name = "dish_id", nullable = false)
    private Integer dishId;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(length = 255)
    private String remark = "";

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false, precision = 10, scale = 2)
    private Double price;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "prepared")
    private Boolean prepared = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Integer getDishId() {
        return dishId;
    }

    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }


    public String getRemark() {
        return remark;
    }


    public void setRemark(String remark) {
        this.remark = remark;
    }


    public Boolean getCompleted() {
        return completed;
    }


    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Double getPrice() {
        return price;
    }


    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 添加租户ID的getter和setter
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
