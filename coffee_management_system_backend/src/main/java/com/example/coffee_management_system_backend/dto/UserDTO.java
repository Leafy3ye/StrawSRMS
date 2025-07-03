package com.example.coffee_management_system_backend.dto;

public class UserDTO {
    private Long id;
    private String username;
    private String avatarUrl;

    // 不包含密码，用于返回给前端
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}