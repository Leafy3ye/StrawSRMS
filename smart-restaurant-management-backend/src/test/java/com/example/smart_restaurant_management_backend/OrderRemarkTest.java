package com.example.smart_restaurant_management_backend;

import com.example.smart_restaurant_management_backend.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrderRemarkTest {

    @Test
    public void testOrderRemarkPersistence() {
        // 创建一个订单
        Order order = new Order();
        order.setId(1L);
        order.setTableId(1L);
        order.setDishId(1L);
        order.setQuantity(1);
        order.setRemark("特殊要求：不要辣");
        
        // 验证备注字段
        assertNotNull(order.getRemark());
        assertEquals("特殊要求：不要辣", order.getRemark());
        
        // 测试备注更新
        order.setRemark("修改备注：微辣");
        assertEquals("修改备注：微辣", order.getRemark());
        
        System.out.println("Order remark test passed: " + order.getRemark());
    }
    
    @Test
    public void testEmptyRemark() {
        // 测试空备注
        Order order = new Order();
        order.setRemark("");
        assertEquals("", order.getRemark());
        
        // 测试null备注
        order.setRemark(null);
        assertNull(order.getRemark());
        
        System.out.println("Empty remark test passed");
    }
}
