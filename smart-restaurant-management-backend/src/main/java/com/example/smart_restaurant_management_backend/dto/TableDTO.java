package com.example.smart_restaurant_management_backend.dto;

import com.example.smart_restaurant_management_backend.model.TableEntity;
import java.time.LocalDateTime;

public class TableDTO {
    private Long id;
    private String name;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 构造函数
    public TableDTO(TableEntity table) {
        this.id = table.getId();
        this.name = table.getName();
        this.status = table.getStatus();
        this.createdAt = table.getCreatedAt();
        this.updatedAt = table.getUpdatedAt();
    }
    
    // Getter 方法
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Setter 方法
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}