package com.example.smart_restaurant_management_backend.service;

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

    public List<TableEntity> findAll() {
        return tableRepository.findAll();
    }

    public Optional<TableEntity> findById(Integer id) {
        return tableRepository.findById(id);
    }

    // 使用原生SQL保存桌位
    public void saveWithSql(String name, String status) {
        tableRepository.insertTableWithSql(name, status);
    }
    
    // 使用原生SQL更新桌位
    public void updateWithSql(Integer id, String name, String status) {
        tableRepository.updateTableWithSql(id, name, status);
    }

    // 保留原有的JPA方法作为备用
    public TableEntity save(TableEntity tableEntity) {
        return tableRepository.save(tableEntity);
    }

    // 使用原生SQL删除桌位
    public void deleteByIdWithSql(Integer id) {
        tableRepository.deleteTableWithSql(id);
    }
    
    // 保留原有的JPA删除方法作为备用
    public void deleteById(Integer id) {
        tableRepository.deleteById(id);
    }

    // 新增方法：获取指定桌位未完成订单的总金额
    public Double getUncompletedOrderTotalAmountByTableId(Integer tableId) {
        return orderRepository.findByTableIdAndCompletedFalse(tableId).stream()
                .mapToDouble(order -> order.getPrice() * order.getQuantity())
                .sum();
    }
}
