package com.example.coffee_management_system_backend.dto;

public class TransferOrderRequest {
    private Integer fromTableId;
    private Integer toTableId;
    
    public TransferOrderRequest() {}
    
    public TransferOrderRequest(Integer fromTableId, Integer toTableId) {
        this.fromTableId = fromTableId;
        this.toTableId = toTableId;
    }
    
    public Integer getFromTableId() {
        return fromTableId;
    }
    
    public void setFromTableId(Integer fromTableId) {
        this.fromTableId = fromTableId;
    }
    
    public Integer getToTableId() {
        return toTableId;
    }
    
    public void setToTableId(Integer toTableId) {
        this.toTableId = toTableId;
    }
}