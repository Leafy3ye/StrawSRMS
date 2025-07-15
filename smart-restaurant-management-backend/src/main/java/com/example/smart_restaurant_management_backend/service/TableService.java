package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.model.TableEntity;
import com.example.smart_restaurant_management_backend.repository.OrderRepository;
import com.example.smart_restaurant_management_backend.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
public class TableService {

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;

    public TableService(TableRepository tableRepository, OrderRepository orderRepository) {
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
    }

    // 获取当前租户的所有桌位
    public List<TableEntity> findAll() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        return tableRepository.findByTenantIdAndStoreId(currentTenantId, currentStoreId);
    }

    // 根据ID查找当前租户的桌位
    public Optional<TableEntity> findById(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        return tableRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
    }

    // 使用原生SQL保存桌位 - 自动设置租户ID和店铺ID
    public void saveWithSql(String name, String status) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        tableRepository.insertTableWithSql(currentTenantId, currentStoreId, name, status);
    }
    
    // 使用原生SQL更新桌位 - 确保只能更新当前租户的桌位
    public void updateWithSql(Long id, String name, String status) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        tableRepository.updateTableWithSql(id, currentTenantId, currentStoreId, name, status);
    }

    // 保留原有的JPA方法作为备用 - 自动设置租户ID和店铺ID
    public TableEntity save(TableEntity tableEntity) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        tableEntity.setTenantId(currentTenantId);
        tableEntity.setStoreId(currentStoreId);
        return tableRepository.save(tableEntity);
    }

    // 使用原生SQL删除桌位 - 确保只能删除当前租户的桌位
    public void deleteByIdWithSql(Integer id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        tableRepository.deleteTableWithSql(id, currentTenantId, currentStoreId);
    }
    
    // 保留原有的JPA删除方法作为备用 - 确保只能删除当前租户的桌位
    public void deleteById(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        
        // 验证桌位是否属于当前租户和店铺
        Optional<TableEntity> table = tableRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
        if (!table.isPresent()) {
            throw new RuntimeException("桌位不存在或无权限访问");
        }
        
        tableRepository.deleteById(id);
    }

    // 获取指定桌位未完成订单的总金额 - 确保只能访问当前租户的数据
    public BigDecimal getUncompletedOrderTotalAmountByTableId(Long tableId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        
        return orderRepository.findByTenantIdAndStoreIdAndTableIdAndCompletedFalse(currentTenantId, currentStoreId, tableId).stream()
                .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 计算指定桌位的营收 - 确保只能访问当前租户的数据
    public double calculateTableRevenue(Long tableId) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或店铺信息");
        }
        
        // 验证桌位是否属于当前租户和店铺
        Optional<TableEntity> table = tableRepository.findByIdAndTenantIdAndStoreId(tableId, currentTenantId, currentStoreId);
        if (!table.isPresent()) {
            throw new RuntimeException("桌位不存在或无权限访问");
        }
        
        // 计算该桌位已完成订单的总金额
        BigDecimal totalRevenue = orderRepository.findByTenantIdAndStoreIdAndTableIdAndCompletedTrue(currentTenantId, currentStoreId, tableId).stream()
                .map(order -> order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalRevenue.doubleValue();
    }
}