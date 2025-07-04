package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {
    // 根据租户ID查询菜品
    List<Dish> findByTenantId(String tenantId);
    
    // 根据租户ID和菜品ID查询
    Optional<Dish> findByIdAndTenantId(Long id, String tenantId);
    
    // 根据租户ID和可用状态查询
    List<Dish> findByTenantIdAndIsAvailable(String tenantId, Boolean isAvailable);
}
