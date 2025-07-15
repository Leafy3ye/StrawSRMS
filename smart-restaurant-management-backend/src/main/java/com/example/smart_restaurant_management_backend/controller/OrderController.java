package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.dto.OrderDetailDTO;
import com.example.smart_restaurant_management_backend.dto.OrderSummaryDTO;
import com.example.smart_restaurant_management_backend.dto.TransferOrderRequest;
import com.example.smart_restaurant_management_backend.model.Order;
import com.example.smart_restaurant_management_backend.model.Transaction;
import com.example.smart_restaurant_management_backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
   // 获取所有订单
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.findAll();
    }
    // 根据桌位ID获取订单
    @GetMapping("/table/{tableId}")
    public List<OrderSummaryDTO> getOrdersByTableId(@PathVariable Long tableId) {
        return orderService.findByTableId(tableId);
    }
    // 获取桌位订单详情（包含菜品信息）
    @GetMapping("/table/{tableId}/details")
    public List<OrderDetailDTO> getOrderDetailsByTableId(@PathVariable Long tableId) {
        return orderService.findOrderDetailsByTableId(tableId);
    }
  // 根据ID获取单个订单
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok) // 找到订单返回200状态
                .orElse(ResponseEntity.notFound().build());
    }// 未找到返回404状态
    // 创建新订单
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.save(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return orderService.findById(id)
                .map(existingOrder -> {
                    existingOrder.setTableId(order.getTableId());
                    existingOrder.setDishId(order.getDishId());
                    existingOrder.setQuantity(order.getQuantity());
                    existingOrder.setRemark(order.getRemark());
                    existingOrder.setCompleted(order.getCompleted());
                    existingOrder.setPrice(order.getPrice());
                    return ResponseEntity.ok(orderService.save(existingOrder));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/table/{tableId}")
    public ResponseEntity<Void> deleteOrdersByTableId(@PathVariable Long tableId) {
        orderService.deleteByTableId(tableId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/checkout/{tableId}")
    public ResponseEntity<Transaction> checkout(@PathVariable Long tableId) {
        try {
            Transaction transaction = orderService.checkout(tableId);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 新增：桌位订单转移接口
    // 在类的末尾添加转移订单的方法
    @PostMapping("/transfer")
    public ResponseEntity<String> transferOrders(@RequestBody TransferOrderRequest request) {
        try {
            orderService.transferOrders(request.getFromTableId(), request.getToTableId());
            return ResponseEntity.ok("订单转移成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 获取订单时间分析数据
    @GetMapping("/time-analysis")
    public ResponseEntity<Map<String, Object>> getOrderTimeAnalysis(
            @RequestParam(defaultValue = "week") String period) {
        try {
            Map<String, Object> result = orderService.getOrderTimeAnalysis(period);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取热门菜品统计
    @GetMapping("/popular-dishes")
    public ResponseEntity<List<Map<String, Object>>> getPopularDishes(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Map<String, Object>> result = orderService.getPopularDishes(limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/chart/week")
    public Map<String, Object> getWeekChartData() {
        return orderService.getOrderChartData("week");
    }

    @GetMapping("/chart/month")
    public Map<String, Object> getMonthChartData() {
        return orderService.getOrderChartData("month");
    }
}
