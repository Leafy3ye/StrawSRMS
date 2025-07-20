package com.example.smart_restaurant_management_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.ResponseEntity;
import com.example.smart_restaurant_management_backend.controller.FileController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FileUploadTest {

    @Test
    public void testDishImageUpload() throws IOException {
        FileController fileController = new FileController();
        
        // 创建模拟图片文件
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test-dish.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        // 测试上传
        ResponseEntity<?> response = fileController.uploadDishImage(mockFile);
        
        // 验证响应
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        
        if (response.getBody() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertTrue(responseBody.containsKey("url"));
            assertTrue(responseBody.get("url").startsWith("/uploads/dishes/"));
            
            System.out.println("Dish image upload test passed: " + responseBody.get("url"));
        }
    }
    
    @Test
    public void testAvatarUpload() throws IOException {
        FileController fileController = new FileController();
        
        // 创建模拟头像文件
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test-avatar.png",
            "image/png",
            "test avatar content".getBytes()
        );
        
        // 测试上传
        ResponseEntity<?> response = fileController.uploadAvatar(mockFile);
        
        // 验证响应
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        
        if (response.getBody() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> responseBody = (Map<String, String>) response.getBody();
            assertTrue(responseBody.containsKey("url"));
            assertTrue(responseBody.get("url").startsWith("/uploads/avatars/"));
            
            System.out.println("Avatar upload test passed: " + responseBody.get("url"));
        }
    }
    
    @Test
    public void testInvalidFileType() {
        FileController fileController = new FileController();
        
        // 创建非图片文件
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );
        
        // 测试上传
        ResponseEntity<?> response = fileController.uploadDishImage(mockFile);
        
        // 验证响应
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("只能上传图片文件", response.getBody());
        
        System.out.println("Invalid file type test passed");
    }
}
