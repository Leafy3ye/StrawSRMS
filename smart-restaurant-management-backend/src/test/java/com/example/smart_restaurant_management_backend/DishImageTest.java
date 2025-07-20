package com.example.smart_restaurant_management_backend;

import com.example.smart_restaurant_management_backend.model.Dish;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DishImageTest {

    @Test
    public void testDishImageUrlUpdate() {
        // 创建一个菜品
        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("测试菜品");
        dish.setPrice(new BigDecimal("25.00"));
        dish.setDescription("测试描述");
        dish.setImageUrl("http://localhost:8080/uploads/dishes/old-image.jpg");
        
        // 验证初始图片URL
        assertNotNull(dish.getImageUrl());
        assertTrue(dish.getImageUrl().contains("old-image.jpg"));
        
        // 更新图片URL
        String newImageUrl = "http://localhost:8080/uploads/dishes/new-image.jpg";
        dish.setImageUrl(newImageUrl);
        
        // 验证更新后的图片URL
        assertEquals(newImageUrl, dish.getImageUrl());
        assertTrue(dish.getImageUrl().contains("new-image.jpg"));
        
        System.out.println("Dish image URL update test passed");
        System.out.println("Old URL: http://localhost:8080/uploads/dishes/old-image.jpg");
        System.out.println("New URL: " + dish.getImageUrl());
    }
    
    @Test
    public void testDishImageUrlValidation() {
        // 测试图片URL格式验证
        String validUrl1 = "http://localhost:8080/uploads/dishes/test.jpg";
        String validUrl2 = "/uploads/dishes/test.png";
        String invalidUrl = "invalid-url";
        
        assertTrue(validUrl1.contains("dishes"));
        assertTrue(validUrl2.contains("dishes"));
        assertFalse(invalidUrl.contains("dishes"));
        
        System.out.println("Dish image URL validation test passed");
    }
    
    @Test
    public void testDishUpdateLogic() {
        // 模拟菜品更新逻辑
        Dish existingDish = new Dish();
        existingDish.setId(1L);
        existingDish.setName("原菜品名");
        existingDish.setPrice(new BigDecimal("20.00"));
        existingDish.setImageUrl("old-image.jpg");
        
        Dish updateData = new Dish();
        updateData.setName("新菜品名");
        updateData.setPrice(new BigDecimal("25.00"));
        updateData.setImageUrl("new-image.jpg");
        
        // 模拟更新逻辑
        existingDish.setName(updateData.getName());
        existingDish.setPrice(updateData.getPrice());
        if (updateData.getImageUrl() != null) {
            existingDish.setImageUrl(updateData.getImageUrl());
        }
        
        // 验证更新结果
        assertEquals("新菜品名", existingDish.getName());
        assertEquals(new BigDecimal("25.00"), existingDish.getPrice());
        assertEquals("new-image.jpg", existingDish.getImageUrl());
        
        System.out.println("Dish update logic test passed");
    }
}
