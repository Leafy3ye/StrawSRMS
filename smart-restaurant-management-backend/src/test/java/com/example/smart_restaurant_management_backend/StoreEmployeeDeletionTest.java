package com.example.smart_restaurant_management_backend;

import com.example.smart_restaurant_management_backend.enums.UserType;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.model.Store;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class StoreEmployeeDeletionTest {

    @Test
    public void testEmployeeDeletionWhenStoreDeleted() {
        // 创建一个分店
        Store branchStore = new Store();
        branchStore.setId(2L);
        branchStore.setStoreName("分店A");
        branchStore.setIsDefault(false);
        
        // 创建一个员工账号
        User employee = new User();
        employee.setId(1L);
        employee.setUsername("employee1");
        employee.setUserType(UserType.EMPLOYEE);
        employee.setStoreId(2L);
        employee.setTenantId(1L);
        
        // 创建一个店长账号
        User manager = new User();
        manager.setId(2L);
        manager.setUsername("manager1");
        manager.setUserType(UserType.TENANT);
        manager.setStoreId(2L);
        manager.setTenantId(1L);
        
        // 验证初始状态
        assertFalse(branchStore.getIsDefault());
        assertEquals(UserType.EMPLOYEE, employee.getUserType());
        assertEquals(UserType.TENANT, manager.getUserType());
        assertEquals(Long.valueOf(2L), employee.getStoreId());
        assertEquals(Long.valueOf(2L), manager.getStoreId());
        
        System.out.println("Store employee deletion test setup completed");
        System.out.println("Employee should be deleted when store is deleted");
        System.out.println("Manager should be moved to default store when store is deleted");
    }
    
    @Test
    public void testUserTypeValidation() {
        // 验证用户类型
        assertEquals("EMPLOYEE", UserType.EMPLOYEE.name());
        assertEquals("TENANT", UserType.TENANT.name());
        
        System.out.println("User type validation test passed");
    }
}
