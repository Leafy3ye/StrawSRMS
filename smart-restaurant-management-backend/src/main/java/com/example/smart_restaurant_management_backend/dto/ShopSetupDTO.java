package com.example.smart_restaurant_management_backend.dto;

public class ShopSetupDTO {
    private String shopName;
    private Integer tableCount;
    
    public ShopSetupDTO() {}
    
    public ShopSetupDTO(String shopName, Integer tableCount) {
        this.shopName = shopName;
        this.tableCount = tableCount;
    }
    
    public String getShopName() {
        return shopName;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    public Integer getTableCount() {
        return tableCount;
    }
    
    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }
}