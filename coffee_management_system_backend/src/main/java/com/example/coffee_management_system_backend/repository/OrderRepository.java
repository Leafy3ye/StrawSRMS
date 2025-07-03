package com.example.coffee_management_system_backend.repository;

import com.example.coffee_management_system_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // 查询指定桌位且未完成的订单
    List<Order> findByTableIdAndCompletedFalse(Integer tableId);
    
    // 根据桌位ID查询所有订单（包括已完成和未完成的）
    List<Order> findByTableId(Integer tableId);
    
    // 根据菜品ID查询订单（检查菜品是否被引用）
    List<Order> findByDishId(Integer dishId);
    
    // 根据菜品ID查询未完成的订单
    List<Order> findByDishIdAndCompletedFalse(Integer dishId);
}
