package com.example.coffee_management_system_backend.service;

import com.example.coffee_management_system_backend.dto.OrderDetailDTO;
import com.example.coffee_management_system_backend.model.Order;
import com.example.coffee_management_system_backend.model.Dish;
import com.example.coffee_management_system_backend.model.Transaction;
import com.example.coffee_management_system_backend.model.TableEntity;
import com.example.coffee_management_system_backend.repository.OrderRepository;
import com.example.coffee_management_system_backend.repository.DishRepository;
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
    }    // 查询所有订单

    public List<Order> findAll() {
        return orderRepository.findAll();
    }// 根据桌位ID查询未完成订单

    public List<Order> findByTableId(Integer tableId) {
        // 调用自定义的查询，获取指定桌位且未完成的订单
        return orderRepository.findByTableIdAndCompletedFalse(tableId);
    }

    public List<OrderDetailDTO> findOrderDetailsByTableId(Integer tableId) {
        List<Order> orders = orderRepository.findByTableIdAndCompletedFalse(tableId);
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

    public Optional<Order> findById(Integer id) {
        return orderRepository.findById(id);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public void deleteById(Integer id) {
        orderRepository.deleteById(id);
    }

    public void deleteByTableId(Integer tableId) {
        // 删除该桌位的所有订单（包括已完成和未完成的）
        List<Order> allOrders = orderRepository.findByTableId(tableId);
        orderRepository.deleteAll(allOrders);
    }
    public Transaction checkout(Integer tableId) {
        // 获取桌位信息
        Optional<TableEntity> tableOpt = tableService.findById(tableId);
        String tableName = tableOpt.map(TableEntity::getName).orElse("桌位" + tableId);
        // 获取未完成的订单
        List<Order> orders = orderRepository.findByTableIdAndCompletedFalse(tableId);
        if (orders.isEmpty()) {
            throw new RuntimeException("没有未完成的订单");
        }
        // 计算总金额
        Double totalAmount = orders.stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
        // 创建交易记录
        Transaction transaction = new Transaction(tableId, tableName, totalAmount, orders.size());
        Transaction savedTransaction = transactionService.save(transaction);
        
        // 结算后直接删除订单记录（而不是标记为已完成）
        orderRepository.deleteAll(orders);
        
        return savedTransaction;
    }
    
    // 新增：桌位订单转移方法
    // 在类的末尾添加以下方法
    public void transferOrders(Integer fromTableId, Integer toTableId) {
        // 验证桌位是否存在
        Optional<TableEntity> fromTable = tableService.findById(fromTableId);
        Optional<TableEntity> toTable = tableService.findById(toTableId);
        
        if (!fromTable.isPresent()) {
            throw new RuntimeException("源桌位不存在");
        }
        if (!toTable.isPresent()) {
            throw new RuntimeException("目标桌位不存在");
        }
        
        // 获取源桌位的未完成订单
        List<Order> ordersToTransfer = orderRepository.findByTableIdAndCompletedFalse(fromTableId);
        
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
