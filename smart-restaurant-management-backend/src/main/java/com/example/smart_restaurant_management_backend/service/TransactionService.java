package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.model.Transaction;
import com.example.smart_restaurant_management_backend.repository.TransactionRepository;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction save(Transaction transaction) {
        // 修复：使用 getCurrentTenantId() 而不是 getCurrentTenantUuid()
        transaction.setTenantId(TenantContext.getCurrentTenantId());
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll() {
        // 修复：使用 getCurrentTenantId() 和对应的 Repository 方法
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        return transactionRepository.findByTenantIdAndStoreId(currentTenantId, currentStoreId);
    }

    public List<Transaction> findTodayTransactions() {
        // 修复：使用正确的方法和参数类型
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        return transactionRepository.findTodayTransactionsByTenantIdAndStoreId(currentTenantId, currentStoreId);
    }

    public Long countTodayTransactions() {
        // 修复：使用正确的方法和参数类型
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        return transactionRepository.countTodayTransactionsByTenantIdAndStoreId(currentTenantId, currentStoreId);
    }

    public BigDecimal sumTodayRevenue() {
        // 修复：使用正确的方法和参数类型
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        BigDecimal result = transactionRepository.sumTodayRevenueByTenantIdAndStoreId(currentTenantId, currentStoreId);
        return result != null ? result : BigDecimal.ZERO;
    }

    public Transaction findById(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        return transactionRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId)
                .orElse(null);
    }
}