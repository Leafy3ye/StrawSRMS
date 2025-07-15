package com.example.smart_restaurant_management_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummaryDTO {
    private Long id;
    private Long tableId;
    private Long dishId;
    private Integer quantity;
    private String remark;
    private Boolean completed;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private Boolean prepared;

    public OrderSummaryDTO() {}

    public OrderSummaryDTO(Long id, Long tableId, Long dishId, Integer quantity, 
                          String remark, Boolean completed, BigDecimal price, 
                          LocalDateTime createdAt, Boolean prepared) {
        this.id = id;
        this.tableId = tableId;
        this.dishId = dishId;
        this.quantity = quantity;
        this.remark = remark;
        this.completed = completed;
        this.price = price;
        this.createdAt = createdAt;
        this.prepared = prepared;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Boolean getPrepared() { return prepared; }
    public void setPrepared(Boolean prepared) { this.prepared = prepared; }
}