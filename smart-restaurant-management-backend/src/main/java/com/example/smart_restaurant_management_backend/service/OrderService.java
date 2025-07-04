package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.dto.OrderDetailDTO;
import com.example.smart_restaurant_management_backend.model.Order;
import com.example.smart_restaurant_management_backend.model.Dish;
import com.example.smart_restaurant_management_backend.model.Transaction;
import com.example.smart_restaurant_management_backend.model.TableEntity;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.repository.OrderRepository;
import com.example.smart_restaurant_management_backend.repository.DishRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final TransactionService transactionService;
    private final TableService tableService;

    public OrderService(OrderRepository orderRepository, DishRepository dishRepository, 
                       TransactionService transactionService, TableService tableService) {
        this.orderRepository = orderRepository;
        this.dishRepository = dishRepository;
        this.transactionService = transactionService;
        this.tableService = tableService;
    }    // 修复：查询所有订单 - 添加租户过滤

    public List<Order> findAll() {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return orderRepository.findByTenantId(currentTenantId);
    }

    // 修复：根据桌位ID查询未完成订单 - 添加租户过滤
    public List<Order> findByTableId(Integer tableId) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return orderRepository.findByTenantIdAndTableIdAndCompletedFalse(currentTenantId, tableId);
    }

    // 修复：查询订单详情 - 添加租户过滤
    public List<OrderDetailDTO> findOrderDetailsByTableId(Integer tableId) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        List<Order> orders = orderRepository.findByTenantIdAndTableIdAndCompletedFalse(currentTenantId, tableId);
        return orders.stream().map(order -> {
            Optional<Dish> dish = dishRepository.findById(order.getDishId().longValue());
            if (dish.isPresent()) {
                return new OrderDetailDTO(
                    order.getId(),
                    order.getTableId(),
                    order.getDishId(),
                    dish.get().getName(),// 关联查询菜品名称
                    dish.get().getPrice(),
                    order.getQuantity(),
                    order.getRemark(),
                    order.getCompleted(),
                    order.getPrice(),
                    order.getCreatedAt()
                );
            } else {
                return new OrderDetailDTO(
                    order.getId(),
                    order.getTableId(),
                    order.getDishId(),
                    "未知菜品",
                    0.0,
                    order.getQuantity(),
                    order.getRemark(),
                    order.getCompleted(),
                    order.getPrice(),
                    order.getCreatedAt()
                );
            }// 处理菜品不存在的情况
        }).collect(Collectors.toList());
    }// 保存订单

    // 修复：根据ID查找订单 - 添加租户过滤
    public Optional<Order> findById(Integer id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return orderRepository.findByIdAndTenantId(id, currentTenantId);
    }

    // 修复：保存订单 - 设置租户ID
    public Order save(Order order) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        order.setTenantId(currentTenantId);
        return orderRepository.save(order);
    }

    // 修复：删除订单 - 添加租户验证
    public void deleteById(Integer id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        Optional<Order> order = orderRepository.findByIdAndTenantId(id, currentTenantId);
        if (order.isPresent()) {
            orderRepository.deleteById(id);
        } else {
            throw new RuntimeException("订单不存在或无权限删除");
        }
    }

    // 修复：删除桌位所有订单 - 添加租户过滤
    public void deleteByTableId(Integer tableId) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        List<Order> allOrders = orderRepository.findByTenantIdAndTableId(currentTenantId, tableId);
        orderRepository.deleteAll(allOrders);
    }

    // 修复：结账方法 - 添加租户过滤
    public Transaction checkout(Integer tableId) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        // 获取桌位信息（TableService已经有租户过滤）
        Optional<TableEntity> tableOpt = tableService.findById(tableId);
        String tableName = tableOpt.map(TableEntity::getName).orElse("桌位" + tableId);
        
        // 获取当前租户的未完成订单
        List<Order> orders = orderRepository.findByTenantIdAndTableIdAndCompletedFalse(currentTenantId, tableId);
        if (orders.isEmpty()) {
            throw new RuntimeException("没有未完成的订单");
        }
        
        // 计算总金额
        Double totalAmount = orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
        
        // 获取租户ID（从订单中获取，假设同一桌位的订单都属于同一租户）
        String tenantId = orders.get(0).getTenantId(); // 需要确保Order类有getTenantId方法
        
        // 创建交易记录 - 修复构造器调用
        Transaction transaction = new Transaction(tenantId, tableId, tableName, totalAmount, orders.size());
        Transaction savedTransaction = transactionService.save(transaction);
        
        // 删除订单记录
        orderRepository.deleteAll(orders);
        
        return savedTransaction;
    }

    // 修复：订单转移 - 添加租户过滤
    public void transferOrders(Integer fromTableId, Integer toTableId) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        // 验证桌位是否存在（TableService已经有租户过滤）
        Optional<TableEntity> fromTable = tableService.findById(fromTableId);
        Optional<TableEntity> toTable = tableService.findById(toTableId);
        
        if (!fromTable.isPresent()) {
            throw new RuntimeException("源桌位不存在");
        }
        if (!toTable.isPresent()) {
            throw new RuntimeException("目标桌位不存在");
        }
        
        // 获取当前租户的源桌位未完成订单
        List<Order> ordersToTransfer = orderRepository.findByTenantIdAndTableIdAndCompletedFalse(currentTenantId, fromTableId);
        
        if (ordersToTransfer.isEmpty()) {
            throw new RuntimeException("源桌位没有未完成的订单");
        }
        
        // 将订单转移到目标桌位
        for (Order order : ordersToTransfer) {
            order.setTableId(toTableId);
        }
        
        // 批量保存更新后的订单
        orderRepository.saveAll(ordersToTransfer);
    }
}
