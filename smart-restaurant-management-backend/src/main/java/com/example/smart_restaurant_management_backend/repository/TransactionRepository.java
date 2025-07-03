package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    // 查询今日交易记录
    @Query("SELECT t FROM Transaction t WHERE DATE(t.createdAt) = CURRENT_DATE")
    List<Transaction> findTodayTransactions();
    
    // 查询指定日期范围的交易记录
    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // 统计今日订单数量
    @Query("SELECT COUNT(t) FROM Transaction t WHERE DATE(t.createdAt) = CURRENT_DATE")
    Long countTodayTransactions();
    
    // 统计今日总收入
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE DATE(t.createdAt) = CURRENT_DATE")
    Double sumTodayRevenue();
}