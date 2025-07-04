package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.model.TableEntity;
import com.example.smart_restaurant_management_backend.repository.OrderRepository;
import com.example.smart_restaurant_management_backend.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return tableRepository.findByTenantId(currentTenantId);
    }

    // 根据ID查找当前租户的桌位
    public Optional<TableEntity> findById(Integer id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return tableRepository.findByIdAndTenantId(id, currentTenantId);
    }

    // 使用原生SQL保存桌位 - 自动设置租户ID
    public void saveWithSql(String name, String status) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        tableRepository.insertTableWithSql(currentTenantId, name, status);
    }
    
    // 使用原生SQL更新桌位 - 确保只能更新当前租户的桌位
    public void updateWithSql(Integer id, String name, String status) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        tableRepository.updateTableWithSql(id, currentTenantId, name, status);
    }

    // 保留原有的JPA方法作为备用 - 自动设置租户ID
    public TableEntity save(TableEntity tableEntity) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        tableEntity.setTenantId(currentTenantId);
        return tableRepository.save(tableEntity);
    }

    // 使用原生SQL删除桌位 - 确保只能删除当前租户的桌位
    public void deleteByIdWithSql(Integer id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        tableRepository.deleteTableWithSql(id, currentTenantId);
    }
    
    // 保留原有的JPA删除方法作为备用 - 确保只能删除当前租户的桌位
    public void deleteById(Integer id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        // 验证桌位是否属于当前租户
        Optional<TableEntity> table = tableRepository.findByIdAndTenantId(id, currentTenantId);
        if (!table.isPresent()) {
            throw new RuntimeException("桌位不存在或无权限访问");
        }
        
        tableRepository.deleteById(id);
    }

    // 获取指定桌位未完成订单的总金额 - 确保只能访问当前租户的数据
    public Double getUncompletedOrderTotalAmountByTableId(Integer tableId) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        return orderRepository.findByTenantIdAndTableIdAndCompletedFalse(currentTenantId, tableId).stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}
