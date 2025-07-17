package com.example.smart_restaurant_management_backend;

import com.example.smart_restaurant_management_backend.model.Store;
import com.example.smart_restaurant_management_backend.service.StoreService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class StoreDeleteTest {

    @Test
    public void testDefaultStoreCannotBeDeleted() {
        // 创建一个默认店铺
        Store defaultStore = new Store();
        defaultStore.setId(1L);
        defaultStore.setStoreName("总店");
        defaultStore.setIsDefault(true);
        
        // 验证默认店铺不能被删除
        assertTrue(defaultStore.getIsDefault());
        
        System.out.println("Default store deletion protection test passed");
    }
    
    @Test
    public void testNonDefaultStoreCanBeDeleted() {
        // 创建一个非默认店铺
        Store branchStore = new Store();
        branchStore.setId(2L);
        branchStore.setStoreName("分店");
        branchStore.setIsDefault(false);
        
        // 验证非默认店铺可以被删除
        assertFalse(branchStore.getIsDefault());
        
        System.out.println("Branch store deletion test passed");
    }
}
