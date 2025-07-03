package com.example.smart_restaurant_management_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class DeductRequestDTO {
    private Long memberId;
    private BigDecimal amount;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    // 新增字段用于邮件内容
    private String tableName;  // 桌位名称
    private List<String> consumeItems;  // 消费项目列表
    private Integer totalItems;  // 消费项目总数
    
    // Getters and Setters
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public List<String> getConsumeItems() {
        return consumeItems;
    }
    
    public void setConsumeItems(List<String> consumeItems) {
        this.consumeItems = consumeItems;
    }
    
    public Integer getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
}