package com.example.smart_restaurant_management_backend.dto;

public class LoginRequestDTO {
    private String username;
    private String password;
    private String userType; // 用户类型：TENANT 或 EMPLOYEE

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}