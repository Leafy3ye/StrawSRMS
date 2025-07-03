package com.example.coffee_management_system_backend.dto;

import java.time.LocalDateTime;

public class OrderDetailDTO {
    private Integer id;
    private Integer tableId;
    private Integer dishId;
    private String dishName;
    private Double dishPrice;
    private Integer quantity;
    private String remark;
    private Boolean completed;
    private Double price;
    private LocalDateTime createdAt;

    // 构造函数
    public OrderDetailDTO() {}

    public OrderDetailDTO(Integer id, Integer tableId, Integer dishId, String dishName, 
                         Double dishPrice, Integer quantity, String remark, 
                         Boolean completed, Double price, LocalDateTime createdAt) {
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

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }

    public Integer getDishId() { return dishId; }
    public void setDishId(Integer dishId) { this.dishId = dishId; }

    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }

    public Double getDishPrice() { return dishPrice; }
    public void setDishPrice(Double dishPrice) { this.dishPrice = dishPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}