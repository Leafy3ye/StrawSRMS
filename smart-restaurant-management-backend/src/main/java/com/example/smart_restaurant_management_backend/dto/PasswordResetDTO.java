package com.example.smart_restaurant_management_backend.dto;

public class PasswordResetDTO {
    private String account;      // 账户（用户名或邮箱）
    private String emailCode;    // 邮箱验证码
    private String newPassword;  // 新密码

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEmailCode() {
        return emailCode;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}