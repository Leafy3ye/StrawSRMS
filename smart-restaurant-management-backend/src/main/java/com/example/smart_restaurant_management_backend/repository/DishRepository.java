package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DishRepository extends JpaRepository<Dish, Long> {
    // 更新：使用Long类型的tenantId和storeId
    List<Dish> findByTenantIdAndStoreId(Long tenantId, Long storeId);
    Optional<Dish> findByIdAndTenantIdAndStoreId(Long id, Long tenantId, Long storeId);
    List<Dish> findByTenantIdAndStoreIdAndIsAvailable(Long tenantId, Long storeId, Boolean isAvailable);

    // 添加只按租户ID查询的方法（用于超级管理员查看租户所有菜品）
    List<Dish> findByTenantId(Long tenantId);
    Optional<Dish> findByIdAndTenantId(Long id, Long tenantId);
    List<Dish> findByTenantIdAndIsAvailable(Long tenantId, Boolean isAvailable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Dish d WHERE d.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Dish d WHERE d.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Dish d WHERE d.tenantId = :tenantId AND d.storeId = :storeId")
    void deleteByTenantIdAndStoreId(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);
}
