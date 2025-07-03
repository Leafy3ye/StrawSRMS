package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.model.Transaction;
import com.example.smart_restaurant_management_backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public List<Transaction> findTodayTransactions() {
        return transactionRepository.findTodayTransactions();
    }

    public Long countTodayTransactions() {
        return transactionRepository.countTodayTransactions();
    }

    public Double sumTodayRevenue() {
        return transactionRepository.sumTodayRevenue();
    }
}