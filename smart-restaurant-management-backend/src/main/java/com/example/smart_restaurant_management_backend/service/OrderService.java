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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

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

    // 获取订单时间分析数据
    public Map<String, Object> getOrderTimeAnalysis(String period) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;
        
        if ("week".equals(period)) {
            startTime = endTime.minusDays(7);
        } else {
            startTime = endTime.minusDays(30);
        }
        
        List<Order> orders = orderRepository.findByTenantIdAndCreatedAtBetween(
            currentTenantId, startTime, endTime);
        
        // 按小时统计订单数量（0-23小时）
        Map<Integer, Integer> hourlyStats = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyStats.put(i, 0);
        }
        
        for (Order order : orders) {
            int hour = order.getCreatedAt().getHour();
            hourlyStats.put(hour, hourlyStats.get(hour) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("hourlyStats", hourlyStats);
        result.put("totalOrders", orders.size());
        result.put("period", period);
        
        return result;
    }

    // 获取热门菜品统计
    public List<Map<String, Object>> getPopularDishes(int limit) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        // 获取所有订单
        List<Order> orders = orderRepository.findByTenantId(currentTenantId);
        
        // 统计每个菜品的销量
        Map<Integer, Integer> dishQuantityMap = new HashMap<>();
        Map<Integer, String> dishNameMap = new HashMap<>();
        
        for (Order order : orders) {
            Integer dishId = order.getDishId();
            dishQuantityMap.put(dishId, 
                dishQuantityMap.getOrDefault(dishId, 0) + order.getQuantity());
            
            // 获取菜品名称（如果还没有缓存）
            if (!dishNameMap.containsKey(dishId)) {
                Optional<Dish> dish = dishRepository.findByIdAndTenantId(
                    dishId.longValue(), currentTenantId);
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
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
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
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        // 计算今日时间范围（使用应用服务器时区）
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return orderRepository.countTodayOrdersByTenantId(currentTenantId, startOfDay, endOfDay);
    }

    public Double getTodayRevenue() {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        // 计算今日时间范围（使用应用服务器时区）
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        Double result = orderRepository.sumTodayRevenueByTenantId(currentTenantId, startOfDay, endOfDay);
        return result != null ? result : 0.0;
    }

    // 获取总订单数量
    public Long getTotalOrderCount() {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        List<Order> allOrders = orderRepository.findByTenantId(currentTenantId);
        return (long) allOrders.size();
    }

    // 获取总收入
    public Double getTotalRevenue() {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        List<Order> allOrders = orderRepository.findByTenantId(currentTenantId);
        return allOrders.stream()
            .mapToDouble(order -> order.getPrice() * order.getQuantity())
            .sum();
    }

    // 获取图表数据（订单趋势）
    public Map<String, Object> getOrderChartData(String period) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findByTenantId(currentTenantId);
        
        Map<String, Object> result = new HashMap<>();
        
        if ("week".equals(period)) {
            // 生成最近7天的数据
            List<String> dates = new ArrayList<>();
            List<Integer> orderCounts = new ArrayList<>();
            List<Double> revenues = new ArrayList<>();
            
            for (int i = 6; i >= 0; i--) {
                LocalDateTime date = now.minusDays(i);
                String dateStr = date.toLocalDate().toString();
                dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                
                List<Order> dayOrders = orders.stream()
                    .filter(order -> order.getCreatedAt().toLocalDate().equals(date.toLocalDate()))
                    .collect(Collectors.toList());
                
                orderCounts.add(dayOrders.size());
                revenues.add(dayOrders.stream()
                    .mapToDouble(order -> order.getPrice() * order.getQuantity())
                    .sum());
            }
            
            result.put("dates", dates);
            result.put("orders", orderCounts);
            result.put("revenue", revenues);
        } else {
            // 生成最近30天的数据（每5天一个点）
            List<String> dates = new ArrayList<>();
            List<Integer> orderCounts = new ArrayList<>();
            List<Double> revenues = new ArrayList<>();
            
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
                    .mapToDouble(order -> order.getPrice() * order.getQuantity())
                    .sum());
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
