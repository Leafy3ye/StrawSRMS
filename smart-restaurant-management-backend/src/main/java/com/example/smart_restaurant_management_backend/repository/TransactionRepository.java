package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 根据租户ID查询交易记录
    List<Transaction> findByTenantId(String tenantId);
    
    // 根据租户ID和交易ID查询
    Optional<Transaction> findByIdAndTenantId(Long id, String tenantId);
    
    // 根据租户ID和桌位ID查询交易记录
    List<Transaction> findByTenantIdAndTableId(String tenantId, Integer tableId);
    
    // 查询今日交易记录
    @Query("SELECT t FROM Transaction t WHERE DATE(t.createdAt) = CURRENT_DATE")
    List<Transaction> findTodayTransactions();
    
    // 统计今日交易数量
    @Query("SELECT COUNT(t) FROM Transaction t WHERE DATE(t.createdAt) = CURRENT_DATE")
    Long countTodayTransactions();
    
    // 计算今日总收入
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0.0) FROM Transaction t WHERE DATE(t.createdAt) = CURRENT_DATE")
    Double sumTodayRevenue();
    
    // 根据租户ID查询今日交易记录
    @Query("SELECT t FROM Transaction t WHERE t.tenantId = ?1 AND DATE(t.createdAt) = CURRENT_DATE")
    List<Transaction> findTodayTransactionsByTenantId(String tenantId);
    
    // 根据租户ID统计今日交易数量
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.tenantId = ?1 AND DATE(t.createdAt) = CURRENT_DATE")
    Long countTodayTransactionsByTenantId(String tenantId);
    
    // 根据租户ID计算今日总收入
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0.0) FROM Transaction t WHERE t.tenantId = ?1 AND DATE(t.createdAt) = CURRENT_DATE")
    Double sumTodayRevenueByTenantId(String tenantId);
}