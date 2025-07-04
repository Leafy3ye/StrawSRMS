package com.example.smart_restaurant_management_backend.dto;

public class DeleteAccountDTO {
    private String confirmText;
    private String currentPassword;
    
    public String getConfirmText() {
        return confirmText;
    }
    
    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
    }
    
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}