package com.example.smart_restaurant_management_backend.dto;

public class EmployeeRegisterDTO {
    private String username;
    private String password;
    private String email;
    private String phone;
    private Long storeId; // 指定员工所属的店铺

    public EmployeeRegisterDTO() {}

    public EmployeeRegisterDTO(String username, String password, String email, String phone, Long storeId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.storeId = storeId;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}
