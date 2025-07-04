package com.example.smart_restaurant_management_backend.dto;

public class RestaurantSetupRequest {
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhone;
    
    // 构造函数
    public RestaurantSetupRequest() {}
    
    public RestaurantSetupRequest(String restaurantName, String restaurantAddress, String restaurantPhone) {
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.restaurantPhone = restaurantPhone;
    }
    
    // Getter 和 Setter 方法
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public String getRestaurantAddress() {
        return restaurantAddress;
    }
    
    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }
    
    public String getRestaurantPhone() {
        return restaurantPhone;
    }
    
    public void setRestaurantPhone(String restaurantPhone) {
        this.restaurantPhone = restaurantPhone;
    }
}