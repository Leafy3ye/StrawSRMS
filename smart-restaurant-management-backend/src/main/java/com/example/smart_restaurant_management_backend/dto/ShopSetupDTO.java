package com.example.smart_restaurant_management_backend.dto;

public class ShopSetupDTO {
    private String shopName;
    private Integer tableCount;
    private String mode; // "single" 或 "multi"
    private String brandName; // 品牌名称

    public ShopSetupDTO() {}

    public ShopSetupDTO(String shopName, Integer tableCount) {
        this.shopName = shopName;
        this.tableCount = tableCount;
    }

    public ShopSetupDTO(String shopName, Integer tableCount, String mode) {
        this.shopName = shopName;
        this.tableCount = tableCount;
        this.mode = mode;
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
}