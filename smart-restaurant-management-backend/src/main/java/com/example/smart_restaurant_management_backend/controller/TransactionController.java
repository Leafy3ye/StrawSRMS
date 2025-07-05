package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.Transaction;
import com.example.smart_restaurant_management_backend.service.TransactionService;
import com.example.smart_restaurant_management_backend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final OrderService orderService;

    public TransactionController(TransactionService transactionService, OrderService orderService) {
        this.transactionService = transactionService;
        this.orderService = orderService;
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
        // 改为从Orders表获取统计数据
        stats.put("todayOrderCount", orderService.getTodayOrderCount());
        stats.put("todayRevenue", orderService.getTodayRevenue());
        stats.put("totalOrderCount", orderService.getTotalOrderCount());
        stats.put("totalRevenue", orderService.getTotalRevenue());
        return stats;
    }
}