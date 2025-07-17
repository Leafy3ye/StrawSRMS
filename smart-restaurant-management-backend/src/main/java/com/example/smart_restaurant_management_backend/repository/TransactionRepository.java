package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 更新：使用Long类型的tenantId和storeId
    List<Transaction> findByTenantIdAndStoreId(Long tenantId, Long storeId);
    Optional<Transaction> findByIdAndTenantIdAndStoreId(Long id, Long tenantId, Long storeId);
    List<Transaction> findByTenantIdAndStoreIdAndTableId(Long tenantId, Long storeId, Long tableId);

    // 时间范围查询
    List<Transaction> findByTenantIdAndStoreIdAndCreatedAtBetween(
        Long tenantId, Long storeId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 统计查询 - 添加storeId过滤
    @Query("SELECT t FROM Transaction t WHERE t.tenantId = ?1 AND t.storeId = ?2 AND DATE(t.createdAt) = CURRENT_DATE")
    List<Transaction> findTodayTransactionsByTenantIdAndStoreId(Long tenantId, Long storeId);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.tenantId = ?1 AND t.storeId = ?2 AND DATE(t.createdAt) = CURRENT_DATE")
    Long countTodayTransactionsByTenantIdAndStoreId(Long tenantId, Long storeId);
    
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0.0) FROM Transaction t WHERE t.tenantId = ?1 AND t.storeId = ?2 AND DATE(t.createdAt) = CURRENT_DATE")
    java.math.BigDecimal sumTodayRevenueByTenantIdAndStoreId(Long tenantId, Long storeId);
    
    // 兼容旧版本的方法（只按租户ID查询）
    List<Transaction> findByTenantId(String tenantId);
    
    @Query("SELECT t FROM Transaction t WHERE t.tenantId = ?1 AND DATE(t.createdAt) = CURRENT_DATE")
    List<Transaction> findTodayTransactionsByTenantId(String tenantId);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.tenantId = ?1 AND DATE(t.createdAt) = CURRENT_DATE")
    Long countTodayTransactionsByTenantId(String tenantId);
    
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0.0) FROM Transaction t WHERE t.tenantId = ?1 AND DATE(t.createdAt) = CURRENT_DATE")
    java.math.BigDecimal sumTodayRevenueByTenantId(String tenantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.tenantId = :tenantId AND t.storeId = :storeId")
    void deleteByTenantIdAndStoreId(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);
}