package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.Transaction;
import com.example.smart_restaurant_management_backend.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        return transactionService.save(transaction);
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.findAll();
    }

    @GetMapping("/today")
    public List<Transaction> getTodayTransactions() {
        return transactionService.findTodayTransactions();
    }

    @GetMapping("/stats")
    public Map<String, Object> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayOrderCount", transactionService.countTodayTransactions());
        stats.put("todayRevenue", transactionService.sumTodayRevenue());
        return stats;
    }
}