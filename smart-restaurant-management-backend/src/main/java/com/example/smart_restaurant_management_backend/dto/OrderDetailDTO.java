package com.example.smart_restaurant_management_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDetailDTO {
    // ID字段 - 从Integer改为Long，与Order实体保持一致
    private Long id;
    
    // 桌位ID - 从Integer改为Long，与Order实体保持一致
    private Long tableId;
    
    // 菜品ID - 改为Long类型，与数据库schema保持一致
    private Long dishId;
    
    // 菜品名称 - 缺失的字段声明
    private String dishName;
    
    // 菜品价格 - 缺失的字段声明
    private BigDecimal dishPrice;
    
    // 数量 - 缺失的字段声明
    private Integer quantity;
    
    // 备注 - 缺失的字段声明
    private String remark;
    
    // 是否完成 - 缺失的字段声明
    private Boolean completed;
    
    // 订单价格 - 缺失的字段声明
    private BigDecimal price;
    
    // 创建时间 - 缺失的字段声明
    private LocalDateTime createdAt;
    
    // 租户ID - 缺失的字段声明
    private Long tenantId;
    
    // 店铺ID - 缺失的字段声明
    private Long storeId;

    // 默认构造函数
    public OrderDetailDTO() {}
    
    // 构造函数参数也需要相应修改
    public OrderDetailDTO(Long id, Long tableId, Long dishId, String dishName, 
                         BigDecimal dishPrice, Integer quantity, String remark, 
                         Boolean completed, BigDecimal price, LocalDateTime createdAt) {
        this.id = id;
        this.tableId = tableId;
        this.dishId = dishId;
        this.dishName = dishName;
        this.dishPrice = dishPrice;
        this.quantity = quantity;
        this.remark = remark;
        this.completed = completed;
        this.price = price;
        this.createdAt = createdAt;
    }
    
    // 完整构造函数 - 包含租户和店铺信息，修正dishId类型
    public OrderDetailDTO(Long id, Long tableId, Long dishId, String dishName, 
                         BigDecimal dishPrice, Integer quantity, String remark, 
                         Boolean completed, BigDecimal price, LocalDateTime createdAt,
                         Long tenantId, Long storeId) {
        this(id, tableId, dishId, dishName, dishPrice, quantity, remark, completed, price, createdAt);
        this.tenantId = tenantId;
        this.storeId = storeId;
    }

    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public Long getTableId() { 
        return tableId; 
    }
    
    public void setTableId(Long tableId) { 
        this.tableId = tableId; 
    }
    
    // getter/setter方法
    public Long getDishId() { 
        return dishId; 
    }
    
    public void setDishId(Long dishId) { 
        this.dishId = dishId; 
    }
    
    public String getDishName() { 
        return dishName; 
    }
    
    public void setDishName(String dishName) { 
        this.dishName = dishName; 
    }
    
    public BigDecimal getDishPrice() { 
        return dishPrice; 
    }
    
    public void setDishPrice(BigDecimal dishPrice) { 
        this.dishPrice = dishPrice; 
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
    
    public BigDecimal getPrice() { 
        return price; 
    }
    
    public void setPrice(BigDecimal price) { 
        this.price = price; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
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
}