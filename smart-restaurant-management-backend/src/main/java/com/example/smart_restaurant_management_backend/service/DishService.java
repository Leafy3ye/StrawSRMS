package com.example.smart_restaurant_management_backend.service;


import com.example.smart_restaurant_management_backend.model.Dish;
import com.example.smart_restaurant_management_backend.model.Order;
import com.example.smart_restaurant_management_backend.repository.DishRepository;
import com.example.smart_restaurant_management_backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DishService {

    @Autowired
    private DishRepository dishRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    // 获取所有菜品
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    // 根据ID查找菜品
    public Optional<Dish> getDishById(Long id) {
        return dishRepository.findById(id);
    }

    // 添加菜品
    public Dish addDish(Dish dish) {
        return dishRepository.save(dish);
    }

    // 编辑菜品
    public Dish updateDish(Long id, Dish dish) {
        // 首先从数据库获取现有菜品
        Optional<Dish> existingDishOpt = dishRepository.findById(id);
        if (existingDishOpt.isPresent()) {
            Dish existingDish = existingDishOpt.get();

            // 只更新需要修改的字段
            existingDish.setName(dish.getName());
            existingDish.setPrice(dish.getPrice());
            existingDish.setDescription(dish.getDescription());
            existingDish.setIsAvailable(dish.getIsAvailable());

            // 保存更新后的菜品
            return dishRepository.save(existingDish);
        } else {
            // 如果没有找到菜品，可以抛出异常或返回 null
            throw new RuntimeException("Dish not found with id " + id);
        }
    }


    // 删除菜品
    public void deleteDish(Long id) {
        // 检查是否有未完成的订单引用此菜品
        List<Order> relatedOrders = orderRepository.findByDishIdAndCompletedFalse(id.intValue());
        
        if (!relatedOrders.isEmpty()) {
            throw new RuntimeException("无法删除菜品：该菜品存在未完成的订单，请先处理相关订单");
        }
        
        // 检查是否有任何订单引用此菜品（包括已完成的）
        List<Order> allRelatedOrders = orderRepository.findByDishId(id.intValue());
        
        if (!allRelatedOrders.isEmpty()) {
            throw new RuntimeException("无法删除菜品：该菜品存在历史订单记录，为保持数据完整性不能删除");
        }
        
        dishRepository.deleteById(id);
    }

    // 强制删除菜品（包括历史记录）
    public void forceDeleteDish(Long id) {
        // 检查是否有未完成的订单
        List<Order> relatedOrders = orderRepository.findByDishIdAndCompletedFalse(id.intValue());
        
        if (!relatedOrders.isEmpty()) {
            throw new RuntimeException("无法删除菜品：该菜品存在未完成的订单，请先处理相关订单");
        }
        
        // 删除所有相关的历史订单记录
        List<Order> allRelatedOrders = orderRepository.findByDishId(id.intValue());
        if (!allRelatedOrders.isEmpty()) {
            orderRepository.deleteAll(allRelatedOrders);
        }
        
        // 删除菜品
        dishRepository.deleteById(id);
    }
}
