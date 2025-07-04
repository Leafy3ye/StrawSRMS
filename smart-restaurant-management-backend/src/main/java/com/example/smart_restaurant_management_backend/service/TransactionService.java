package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.model.Transaction;
import com.example.smart_restaurant_management_backend.repository.TransactionRepository;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction save(Transaction transaction) {
        // 设置当前租户ID
        transaction.setTenantId(TenantContext.getCurrentTenantUuid());
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll() {
        // 只返回当前租户的交易记录
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        return transactionRepository.findByTenantId(currentTenantId);
    }

    public List<Transaction> findTodayTransactions() {
        // 只返回当前租户今日的交易记录
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        return transactionRepository.findTodayTransactionsByTenantId(currentTenantId);
    }

    public Long countTodayTransactions() {
        // 只统计当前租户今日的交易数量
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        return transactionRepository.countTodayTransactionsByTenantId(currentTenantId);
    }

    public Double sumTodayRevenue() {
        // 只计算当前租户今日的总收入
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        return transactionRepository.sumTodayRevenueByTenantId(currentTenantId);
    }
}