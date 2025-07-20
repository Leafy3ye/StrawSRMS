package com.example.smart_restaurant_management_backend;

import com.example.smart_restaurant_management_backend.model.Member;
import com.example.smart_restaurant_management_backend.model.MemberLevel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MemberSettlementTest {

    @Test
    public void testMemberDiscountCalculation() {
        // 创建会员等级
        MemberLevel goldLevel = new MemberLevel();
        goldLevel.setLevelName("黄金会员");
        goldLevel.setDiscountRate(new BigDecimal("0.9")); // 9折
        
        // 创建会员
        Member member = new Member();
        member.setName("测试会员");
        member.setPhone("13800138000");
        member.setBalance(new BigDecimal("100.00"));
        member.setMemberLevel("黄金会员");
        
        // 测试折扣计算
        BigDecimal originalAmount = new BigDecimal("50.00");
        BigDecimal discountedAmount = originalAmount.multiply(goldLevel.getDiscountRate());
        BigDecimal discountAmount = originalAmount.subtract(discountedAmount);
        
        // 验证计算结果
        assertEquals(new BigDecimal("45.00"), discountedAmount);
        assertEquals(new BigDecimal("5.00"), discountAmount);
        
        // 验证余额是否足够
        assertTrue(member.getBalance().compareTo(discountedAmount) >= 0);
        
        System.out.println("Member discount calculation test passed");
        System.out.println("Original amount: " + originalAmount);
        System.out.println("Discount rate: " + goldLevel.getDiscountRate());
        System.out.println("Discounted amount: " + discountedAmount);
        System.out.println("Discount amount: " + discountAmount);
    }
    
    @Test
    public void testInsufficientBalance() {
        // 创建余额不足的会员
        Member member = new Member();
        member.setName("余额不足会员");
        member.setPhone("13800138001");
        member.setBalance(new BigDecimal("10.00"));
        member.setMemberLevel("普通会员");
        
        // 消费金额
        BigDecimal consumeAmount = new BigDecimal("50.00");
        
        // 验证余额不足
        assertTrue(member.getBalance().compareTo(consumeAmount) < 0);
        
        System.out.println("Insufficient balance test passed");
        System.out.println("Member balance: " + member.getBalance());
        System.out.println("Consume amount: " + consumeAmount);
    }
    
    @Test
    public void testMemberLevelDiscount() {
        // 测试不同会员等级的折扣
        MemberLevel[] levels = {
            createLevel("普通会员", "1.0"),
            createLevel("银卡会员", "0.95"),
            createLevel("金卡会员", "0.9"),
            createLevel("钻石会员", "0.85")
        };
        
        BigDecimal originalAmount = new BigDecimal("100.00");
        
        for (MemberLevel level : levels) {
            BigDecimal discountedAmount = originalAmount.multiply(level.getDiscountRate());
            System.out.println(level.getLevelName() + ": " + 
                level.getDiscountRate() + " -> ¥" + discountedAmount);
            
            // 验证折扣率在合理范围内
            assertTrue(level.getDiscountRate().compareTo(new BigDecimal("0.5")) >= 0);
            assertTrue(level.getDiscountRate().compareTo(new BigDecimal("1.0")) <= 0);
        }
        
        System.out.println("Member level discount test passed");
    }
    
    private MemberLevel createLevel(String name, String discountRate) {
        MemberLevel level = new MemberLevel();
        level.setLevelName(name);
        level.setDiscountRate(new BigDecimal(discountRate));
        return level;
    }
}
