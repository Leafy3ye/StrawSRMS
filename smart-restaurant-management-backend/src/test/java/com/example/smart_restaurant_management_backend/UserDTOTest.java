package com.example.smart_restaurant_management_backend;

import com.example.smart_restaurant_management_backend.dto.UserDTO;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.enums.UserType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class UserDTOTest {

    @Test
    public void testUserDTOCreatedAtField() {
        // 创建一个测试用户
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone("1234567890");
        user.setUserType(UserType.EMPLOYEE);

        // 模拟 @PrePersist 调用，手动设置创建时间
        LocalDateTime now = LocalDateTime.now();
        try {
            // 使用反射设置 createdAt 字段
            java.lang.reflect.Field createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, now);
        } catch (Exception e) {
            fail("Failed to set createdAt field: " + e.getMessage());
        }

        // 创建 UserDTO
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setUserType(user.getUserType().name());
        dto.setCreatedAt(user.getCreatedAt());

        // 验证 createdAt 字段是否正确设置
        assertNotNull(dto.getCreatedAt());
        assertEquals(now, dto.getCreatedAt());

        System.out.println("UserDTO createdAt test passed: " + dto.getCreatedAt());
    }
}
