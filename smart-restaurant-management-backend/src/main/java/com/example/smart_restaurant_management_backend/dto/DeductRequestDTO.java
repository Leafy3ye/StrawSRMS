package com.example.smart_restaurant_management_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class DeductRequestDTO {
    // 会员ID - 保持Long类型（已经是正确的）
    private Long memberId;
    
    // 扣款金额
    private BigDecimal amount;
    
    // 租户ID - 新增，改为Long类型
    private Long tenantId;
    
    // 店铺ID - 新增，用于店铺级别隔离
    private Long storeId;

    // 邮件内容相关字段
    private String tableName;  // 桌位名称
    private List<String> consumeItems;  // 消费项目列表
    private Integer totalItems;  // 消费项目总数
    
    // 默认构造函数
    public DeductRequestDTO() {}
    
    // 带参构造函数
    public DeductRequestDTO(Long memberId, BigDecimal amount, Long tenantId, Long storeId) {
        this.memberId = memberId;
        this.amount = amount;
        this.tenantId = tenantId;
        this.storeId = storeId;
    }

    // Getters and Setters
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