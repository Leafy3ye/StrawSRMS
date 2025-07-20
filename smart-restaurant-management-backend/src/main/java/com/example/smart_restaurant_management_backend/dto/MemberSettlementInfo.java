package com.example.smart_restaurant_management_backend.dto;

import java.math.BigDecimal;

public class MemberSettlementInfo {
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private String memberLevel;
    private BigDecimal originalAmount;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;

    // 默认构造函数
    public MemberSettlementInfo() {}

    // 全参构造函数
    public MemberSettlementInfo(Long memberId, String memberName, String memberPhone, String memberLevel,
                               BigDecimal originalAmount, BigDecimal discountRate, BigDecimal discountAmount, BigDecimal actualAmount) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberPhone = memberPhone;
        this.memberLevel = memberLevel;
        this.originalAmount = originalAmount;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.actualAmount = actualAmount;
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public void setMemberPhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(String memberLevel) {
        this.memberLevel = memberLevel;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    @Override
    public String toString() {
        return "MemberSettlementInfo{" +
                "memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", memberPhone='" + memberPhone + '\'' +
                ", memberLevel='" + memberLevel + '\'' +
                ", originalAmount=" + originalAmount +
                ", discountRate=" + discountRate +
                ", discountAmount=" + discountAmount +
                ", actualAmount=" + actualAmount +
                '}';
    }
}
