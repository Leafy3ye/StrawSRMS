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

    @GetMapping("/{id}/details")
    public Map<String, Object> getTransactionDetails(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取交易记录
            Transaction transaction = transactionService.findById(id);
            if (transaction == null) {
                result.put("error", "交易记录不存在");
                return result;
            }

            // 构建交易详情
            Map<String, Object> transactionInfo = new HashMap<>();
            transactionInfo.put("id", transaction.getId());
            transactionInfo.put("tableId", transaction.getTableId());
            transactionInfo.put("tableName", transaction.getTableName());
            transactionInfo.put("totalAmount", transaction.getTotalAmount());
            transactionInfo.put("orderCount", transaction.getOrderCount());
            transactionInfo.put("createdAt", transaction.getCreatedAt());

            // 获取对应的订单详情（基于交易ID）
            List<Map<String, Object>> orderDetails = orderService.getOrdersByTransactionId(
                transaction.getId()
            );

            // 如果基于交易ID没有找到订单（兼容旧数据），则使用时间范围查询
            if (orderDetails.isEmpty()) {
                orderDetails = orderService.getCompletedOrdersByTableAndTime(
                    transaction.getTableId(),
                    transaction.getCreatedAt()
                );
            }

            result.put("transaction", transactionInfo);
            result.put("orders", orderDetails);

            return result;
        } catch (Exception e) {
            result.put("error", "获取交易详情失败: " + e.getMessage());
            return result;
        }
    }
}