package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 更新：使用Long类型的tenantId和storeId
    List<Order> findByTenantIdAndStoreIdAndTableIdAndCompletedFalse(Long tenantId, Long storeId, Long tableId);
    // 添加这个方法
    List<Order> findByTenantIdAndStoreIdAndTableIdAndCompletedTrue(Long tenantId, Long storeId, Long tableId);
    List<Order> findByTenantIdAndStoreIdAndTableId(Long tenantId, Long storeId, Long tableId);
    List<Order> findByTenantIdAndStoreIdAndDishId(Long tenantId, Long storeId, Long dishId);
    
    // 新增：添加缺失的方法，注意 dishId 应该是 Long 类型
    List<Order> findByTenantIdAndStoreIdAndDishIdAndCompletedFalse(Long tenantId, Long storeId, Long dishId);
    
    // 兼容旧版本的方法（只按租户ID和桌子ID查询）
    List<Order> findByTenantIdAndTableIdAndCompletedFalse(String tenantId, Long tableId);
    
    List<Order> findByTenantIdAndStoreId(Long tenantId, Long storeId);
    Optional<Order> findByIdAndTenantIdAndStoreId(Long id, Long tenantId, Long storeId);
    
    // 时间范围查询
    List<Order> findByTenantIdAndStoreIdAndCreatedAtBetween(
        Long tenantId, Long storeId, LocalDateTime startTime, LocalDateTime endTime);

    // 统计查询
    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = :tenantId AND o.storeId = :storeId AND o.createdAt >= :startTime AND o.createdAt < :endTime")
    Long countTodayOrdersByTenantIdAndStoreId(@Param("tenantId") Long tenantId, 
                                             @Param("storeId") Long storeId,
                                             @Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COALESCE(SUM(o.price * o.quantity), 0) FROM Order o WHERE o.tenantId = :tenantId AND o.storeId = :storeId AND o.createdAt >= :startTime AND o.createdAt < :endTime")
    java.math.BigDecimal sumTodayRevenueByTenantIdAndStoreId(@Param("tenantId") Long tenantId, 
                                              @Param("storeId") Long storeId,
                                              @Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM Order o WHERE o.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Order o WHERE o.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}
