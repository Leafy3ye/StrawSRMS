package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.dto.OrderDetailDTO;
import com.example.smart_restaurant_management_backend.model.Order;
import com.example.smart_restaurant_management_backend.model.Dish;
import com.example.smart_restaurant_management_backend.model.Transaction;
import com.example.smart_restaurant_management_backend.model.TableEntity;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.repository.OrderRepository;
import com.example.smart_restaurant_management_backend.repository.DishRepository;
import com.example.smart_restaurant_management_backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
// 在文件顶部添加 import
import com.example.smart_restaurant_management_backend.dto.OrderSummaryDTO;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final TableService tableService;

    public OrderService(OrderRepository orderRepository, DishRepository dishRepository,
                       TransactionService transactionService, TransactionRepository transactionRepository,
                       TableService tableService) {
        this.orderRepository = orderRepository;
        this.dishRepository = dishRepository;
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.tableService = tableService;
    }

    // 查询所有订单 - 支持租户和店铺级别过滤
    public List<Order> findAll() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();

        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }

        // 如果有店铺ID，按租户和店铺查询；否则按租户查询所有订单
        if (currentStoreId != null) {
            return orderRepository.findByTenantIdAndStoreId(currentTenantId, currentStoreId);
        } else {
            return orderRepository.findByTenantId(currentTenantId);
        }
    }

    // 根据桌位ID查询未完成订单 - 支持租户和店铺级别过滤
    // 修改 findByTableId 方法
    public List<OrderSummaryDTO> findByTableId(Long tableId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        List<Order> orders = orderRepository.findByTenantIdAndStoreIdAndTableIdAndCompletedFalse(currentTenantId, currentStoreId, tableId);
        return orders.stream().map(order -> new OrderSummaryDTO(
            order.getId(),
            order.getTableId(),
            order.getDishId(),
            order.getQuantity(),
            order.getRemark(),
            order.getCompleted(),
            order.getPrice(),
            order.getCreatedAt(),
            order.getPrepared()
        )).collect(Collectors.toList());
    }

    // 查询订单详情 - 支持租户和店铺级别过滤
    public List<OrderDetailDTO> findOrderDetailsByTableId(Long tableId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        List<Order> orders = orderRepository.findByTenantIdAndStoreIdAndTableIdAndCompletedFalse(currentTenantId, currentStoreId, tableId);
        return orders.stream().map(order -> {
            Optional<Dish> dish = dishRepository.findByIdAndTenantIdAndStoreId(order.getDishId(), currentTenantId, currentStoreId);
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
                    BigDecimal.ZERO,  // 修复：将 0.0 改为 BigDecimal.ZERO
                    order.getQuantity(),
                    order.getRemark(),
                    order.getCompleted(),
                    order.getPrice(),
                    order.getCreatedAt()
                );
            }// 处理菜品不存在的情况
        }).collect(Collectors.toList());
    }

    // 根据ID查找订单 - 支持租户和店铺级别过滤
    public Optional<Order> findById(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        return orderRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
    }

    // 保存订单 - 设置租户ID和店铺ID
    public Order save(Order order) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        order.setTenantId(currentTenantId);
        order.setStoreId(currentStoreId);
        return orderRepository.save(order);
    }

    // 删除订单 - 支持租户和店铺级别验证
    public void deleteById(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        Optional<Order> order = orderRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
        if (order.isPresent()) {
            orderRepository.deleteById(id);
        } else {
            throw new RuntimeException("订单不存在或无权限删除");
        }
    }

    // 删除桌位所有订单 - 支持租户和店铺级别过滤
    public void deleteByTableId(Long tableId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        List<Order> allOrders = orderRepository.findByTenantIdAndStoreIdAndTableId(currentTenantId, currentStoreId, tableId);
        orderRepository.deleteAll(allOrders);
    }

    // 结账方法 - 支持租户和店铺级别过滤
    public Transaction checkout(Long tableId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        
        // 获取桌位信息（TableService已经有租户过滤）
        Optional<TableEntity> tableOpt = tableService.findById(tableId);
        String tableName = tableOpt.map(TableEntity::getName).orElse("桌位" + tableId);
        
        // 获取当前租户和店铺的未完成订单
        List<Order> orders = orderRepository.findByTenantIdAndStoreIdAndTableIdAndCompletedFalse(currentTenantId, currentStoreId, tableId);
        if (orders.isEmpty()) {
            throw new RuntimeException("没有未完成的订单");
        }
        
        // 计算总金额
        BigDecimal totalAmount = orders.stream()
                .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 创建交易记录
        Transaction transaction = new Transaction(currentTenantId, currentStoreId, tableId, tableName, totalAmount, orders.size());
        Transaction savedTransaction = transactionService.save(transaction);

        // 标记订单为已完成，并设置交易ID
        for (Order order : orders) {
            order.setCompleted(true);
            order.setTransactionId(savedTransaction.getId());
        }
        orderRepository.saveAll(orders);

        return savedTransaction;
    }

    // 根据交易ID获取订单详情
    public List<Map<String, Object>> getOrdersByTransactionId(Long transactionId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }

        List<Order> orders = orderRepository.findByTransactionIdAndTenantIdAndStoreId(
            transactionId, currentTenantId, currentStoreId);

        return orders.stream().map(order -> {
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("id", order.getId());
            orderInfo.put("dishId", order.getDishId());
            orderInfo.put("quantity", order.getQuantity());
            orderInfo.put("price", order.getPrice());
            orderInfo.put("remark", order.getRemark());
            orderInfo.put("createdAt", order.getCreatedAt());

            // 获取菜品信息
            Optional<Dish> dish = dishRepository.findByIdAndTenantIdAndStoreId(
                order.getDishId(), currentTenantId, currentStoreId);
            if (dish.isPresent()) {
                orderInfo.put("dishName", dish.get().getName());
                orderInfo.put("dishPrice", dish.get().getPrice());
            } else {
                orderInfo.put("dishName", "未知菜品");
                orderInfo.put("dishPrice", BigDecimal.ZERO);
            }

            return orderInfo;
        }).collect(Collectors.toList());
    }

    // 根据桌位ID和时间获取已完成的订单详情
    public List<Map<String, Object>> getCompletedOrdersByTableAndTime(Long tableId, LocalDateTime transactionTime) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }

        // 查找在交易时间前后5分钟内完成的订单
        LocalDateTime startTime = transactionTime.minusMinutes(5);
        LocalDateTime endTime = transactionTime.plusMinutes(5);

        List<Order> orders = orderRepository.findByTenantIdAndStoreIdAndTableIdAndCompletedTrueAndCreatedAtBetween(
            currentTenantId, currentStoreId, tableId, startTime, endTime);

        return orders.stream().map(order -> {
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("id", order.getId());
            orderInfo.put("dishId", order.getDishId());
            orderInfo.put("quantity", order.getQuantity());
            orderInfo.put("price", order.getPrice());
            orderInfo.put("remark", order.getRemark());
            orderInfo.put("createdAt", order.getCreatedAt());

            // 获取菜品信息
            Optional<Dish> dish = dishRepository.findByIdAndTenantIdAndStoreId(
                order.getDishId(), currentTenantId, currentStoreId);
            if (dish.isPresent()) {
                orderInfo.put("dishName", dish.get().getName());
                orderInfo.put("dishPrice", dish.get().getPrice());
            } else {
                orderInfo.put("dishName", "未知菜品");
                orderInfo.put("dishPrice", BigDecimal.ZERO);
            }

            return orderInfo;
        }).collect(Collectors.toList());
    }

    // 订单转移 - 支持租户和店铺级别过滤
    public void transferOrders(Long fromTableId, Long toTableId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
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
        
        // 获取当前租户和店铺的源桌位未完成订单
        List<Order> ordersToTransfer = orderRepository.findByTenantIdAndStoreIdAndTableIdAndCompletedFalse(currentTenantId, currentStoreId, fromTableId);
        
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

    // 获取订单时间分析数据
    public Map<String, Object> getOrderTimeAnalysis(String period) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;

        if ("week".equals(period)) {
            startTime = endTime.minusDays(7);
        } else {
            startTime = endTime.minusDays(30);
        }

        // 使用交易记录来统计订单数量，因为一个交易代表一个完整的订单
        List<Transaction> transactions = transactionRepository.findByTenantIdAndStoreIdAndCreatedAtBetween(
            currentTenantId, currentStoreId, startTime, endTime);

        // 按小时统计订单数量（0-23小时）
        Map<Integer, Integer> hourlyStats = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyStats.put(i, 0);
        }

        for (Transaction transaction : transactions) {
            int hour = transaction.getCreatedAt().getHour();
            hourlyStats.put(hour, hourlyStats.get(hour) + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hourlyStats", hourlyStats);
        result.put("totalOrders", transactions.size());
        result.put("period", period);

        return result;
    }

    // 获取热门菜品统计
    public List<Map<String, Object>> getPopularDishes(int limit) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        
        // 获取所有已完成的订单
        List<Order> orders = orderRepository.findByTenantIdAndStoreIdAndCompletedTrue(currentTenantId, currentStoreId);
        
        // 统计每个菜品的销量
        Map<Long, Integer> dishQuantityMap = new HashMap<>();
        Map<Long, String> dishNameMap = new HashMap<>();
        
        for (Order order : orders) {
            Long dishId = order.getDishId();
            dishQuantityMap.put(dishId, 
                dishQuantityMap.getOrDefault(dishId, 0) + order.getQuantity());
            
            // 获取菜品名称（如果还没有缓存）
            if (!dishNameMap.containsKey(dishId)) {
                Optional<Dish> dish = dishRepository.findByIdAndTenantIdAndStoreId(
                    dishId, currentTenantId, currentStoreId);
                if (dish.isPresent()) {
                    dishNameMap.put(dishId, dish.get().getName());
                }
            }
        }
        
        // 如果数据量不足，返回提示信息
        if (dishQuantityMap.size() < 3) {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> noDataMsg = new HashMap<>();
            noDataMsg.put("message", "缺少数据样本，先营业一段时间看看吧");
            result.add(noDataMsg);
            return result;
        }
        
        // 按销量排序并取前N名
        return dishQuantityMap.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("dishId", entry.getKey());
                item.put("dishName", dishNameMap.get(entry.getKey()));
                item.put("quantity", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    // 获取今日订单数量
    public Long getTodayOrderCount() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        
        // 计算今日时间范围（使用应用服务器时区）
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return orderRepository.countTodayOrdersByTenantIdAndStoreId(currentTenantId, currentStoreId, startOfDay, endOfDay);
    }

    public BigDecimal getTodayRevenue() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        
        // 计算今日时间范围（使用应用服务器时区）
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        BigDecimal result = orderRepository.sumTodayRevenueByTenantIdAndStoreId(currentTenantId, currentStoreId, startOfDay, endOfDay);
        return result != null ? result : BigDecimal.ZERO;
    }

    // 获取总订单数量
    public Long getTotalOrderCount() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        
        List<Order> allOrders = orderRepository.findByTenantIdAndStoreId(currentTenantId, currentStoreId);
        return (long) allOrders.size();
    }

    // 获取总收入
    public BigDecimal getTotalRevenue() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        
        List<Order> allOrders = orderRepository.findByTenantIdAndStoreId(currentTenantId, currentStoreId);
        return allOrders.stream()
            .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 获取图表数据（订单趋势）
    public Map<String, Object> getOrderChartData(String period) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        if (currentStoreId == null) {
            throw new RuntimeException("未找到当前店铺信息");
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findByTenantIdAndStoreIdAndCompletedTrue(currentTenantId, currentStoreId);
        
        Map<String, Object> result = new HashMap<>();
        
        if ("week".equals(period)) {
            // 生成最近7天的数据
            List<String> dates = new ArrayList<>();
            List<Integer> orderCounts = new ArrayList<>();
            List<BigDecimal> revenues = new ArrayList<>();
            
            for (int i = 6; i >= 0; i--) {
                LocalDateTime date = now.minusDays(i);
                String dateStr = date.toLocalDate().toString();
                dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                
                List<Order> dayOrders = orders.stream()
                    .filter(order -> order.getCreatedAt().toLocalDate().equals(date.toLocalDate()))
                    .collect(Collectors.toList());
                
                orderCounts.add(dayOrders.size());
                revenues.add(dayOrders.stream()
                    .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            }
            
            result.put("dates", dates);
            result.put("orders", orderCounts);
            result.put("revenue", revenues);
        } else {
            // 生成最近30天的数据（每5天一个点）
            List<String> dates = new ArrayList<>();
            List<Integer> orderCounts = new ArrayList<>();
            List<BigDecimal> revenues = new ArrayList<>();
            
            for (int i = 25; i >= 0; i -= 5) {
                LocalDateTime endDate = now.minusDays(i);
                LocalDateTime startDate = endDate.minusDays(4);
                dates.add(endDate.format(DateTimeFormatter.ofPattern("MM-dd")));
                
                List<Order> periodOrders = orders.stream()
                    .filter(order -> {
                        LocalDate orderDate = order.getCreatedAt().toLocalDate();
                        return !orderDate.isBefore(startDate.toLocalDate()) && 
                               !orderDate.isAfter(endDate.toLocalDate());
                    })
                    .collect(Collectors.toList());
                
                orderCounts.add(periodOrders.size());
                revenues.add(periodOrders.stream()
                    .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            }
            
            result.put("dates", dates);
            result.put("orders", orderCounts);
            result.put("revenue", revenues);
        }
        
        return result;
    }
    
    // 获取收入图表数据
    public Map<String, Object> getRevenueChartData(String period) {
        // 复用订单图表数据，只返回收入相关部分
        return getOrderChartData(period);
    }
}