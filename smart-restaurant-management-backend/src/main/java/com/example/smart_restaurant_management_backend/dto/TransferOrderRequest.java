package com.example.smart_restaurant_management_backend.dto;

public class TransferOrderRequest {
    private Long fromTableId;
    private Long toTableId;
    private Long tenantId;
    private Long storeId;
    
    public TransferOrderRequest() {}
    
    public TransferOrderRequest(Long fromTableId, Long toTableId, Long tenantId, Long storeId) {
        this.fromTableId = fromTableId;
        this.toTableId = toTableId;
        this.tenantId = tenantId;
        this.storeId = storeId;
    }
    
    public Long getFromTableId() {
        return fromTableId;
    }
    
    public void setFromTableId(Long fromTableId) {
        this.fromTableId = fromTableId;
    }
    
    public Long getToTableId() {
        return toTableId;
    }
    
    public void setToTableId(Long toTableId) {
        this.toTableId = toTableId;
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