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
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // 租户相关的查询方法
    List<Order> findByTenantIdAndTableIdAndCompletedFalse(String tenantId, Integer tableId);
    List<Order> findByTenantIdAndTableId(String tenantId, Integer tableId);
    List<Order> findByTenantIdAndDishId(String tenantId, Integer dishId);
    List<Order> findByTenantIdAndDishIdAndCompletedFalse(String tenantId, Integer dishId);
    List<Order> findByTenantId(String tenantId);
    Optional<Order> findByIdAndTenantId(Integer id, String tenantId);
    
    // 兼容旧方法（但建议逐步替换）
    List<Order> findByTableIdAndCompletedFalse(Integer tableId);
    List<Order> findByTableId(Integer tableId);
    List<Order> findByDishId(Integer dishId);
    List<Order> findByDishIdAndCompletedFalse(Integer dishId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Order o WHERE o.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") String tenantId);

    // 根据租户ID和时间范围查询订单
    List<Order> findByTenantIdAndCreatedAtBetween(
        String tenantId, LocalDateTime startTime, LocalDateTime endTime);

    // 修复：使用时间范围查询今日订单数量
    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = :tenantId AND o.createdAt >= :startTime AND o.createdAt < :endTime")
    Long countTodayOrdersByTenantId(@Param("tenantId") String tenantId, 
                                   @Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    // 修复：使用时间范围查询今日营收
    @Query("SELECT COALESCE(SUM(o.price * o.quantity), 0) FROM Order o WHERE o.tenantId = :tenantId AND o.createdAt >= :startTime AND o.createdAt < :endTime")
    Double sumTodayRevenueByTenantId(@Param("tenantId") String tenantId, 
                                    @Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);
}
