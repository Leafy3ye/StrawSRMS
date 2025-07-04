package com.example.smart_restaurant_management_backend.service;


import com.example.smart_restaurant_management_backend.context.TenantContext;
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

    // 获取当前租户的所有菜品
    public List<Dish> getAllDishes() {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return dishRepository.findByTenantId(currentTenantId);
    }

    // 根据ID查找当前租户的菜品
    public Optional<Dish> getDishById(Long id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return dishRepository.findByIdAndTenantId(id, currentTenantId);
    }

    // 添加菜品 - 自动设置租户ID
    public Dish addDish(Dish dish) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        dish.setTenantId(currentTenantId);
        return dishRepository.save(dish);
    }

    // 编辑菜品 - 确保只能编辑当前租户的菜品
    public Dish updateDish(Long id, Dish dish) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        Optional<Dish> existingDishOpt = dishRepository.findByIdAndTenantId(id, currentTenantId);
        if (existingDishOpt.isPresent()) {
            Dish existingDish = existingDishOpt.get();
            existingDish.setName(dish.getName());
            existingDish.setPrice(dish.getPrice());
            existingDish.setDescription(dish.getDescription());
            existingDish.setIsAvailable(dish.getIsAvailable());
            return dishRepository.save(existingDish);
        } else {
            throw new RuntimeException("菜品不存在或无权限访问");
        }
    }

    // 删除菜品 - 确保只能删除当前租户的菜品
    public void deleteDish(Long id) {
        String currentTenantId = TenantContext.getCurrentTenantUuid();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        
        // 检查是否有未完成的订单引用此菜品
        List<Order> relatedOrders = orderRepository.findByTenantIdAndDishIdAndCompletedFalse(currentTenantId, id.intValue());
        
        if (!relatedOrders.isEmpty()) {
            throw new RuntimeException("无法删除菜品：该菜品存在未完成的订单，请先处理相关订单");
        }
        
        // 检查菜品是否属于当前租户
        Optional<Dish> dish = dishRepository.findByIdAndTenantId(id, currentTenantId);
        if (!dish.isPresent()) {
            throw new RuntimeException("菜品不存在或无权限访问");
        }
        
        dishRepository.deleteById(id);
    }
}
