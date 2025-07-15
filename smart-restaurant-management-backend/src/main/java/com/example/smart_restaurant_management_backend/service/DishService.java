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

    // 获取当前租户和门店的所有菜品
    public List<Dish> getAllDishes() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        return dishRepository.findByTenantIdAndStoreId(currentTenantId, currentStoreId);
    }

    // 根据ID查找当前租户和门店的菜品
    public Optional<Dish> getDishById(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        return dishRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
    }

    // 添加菜品 - 自动设置租户ID和门店ID
    public Dish addDish(Dish dish) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        dish.setTenantId(currentTenantId);
        dish.setStoreId(currentStoreId);
        return dishRepository.save(dish);
    }

    // 编辑菜品 - 确保只能编辑当前租户和门店的菜品
    public Dish updateDish(Long id, Dish dish) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        
        Optional<Dish> existingDishOpt = dishRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
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

    // 删除菜品 - 确保只能删除当前租户和门店的菜品
    public void deleteDish(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        Long currentStoreId = TenantContext.getCurrentStoreId();
        if (currentTenantId == null || currentStoreId == null) {
            throw new RuntimeException("未找到当前租户或门店信息");
        }
        
        // 修改：直接使用 Long 类型的 id，不需要转换为 int
        List<Order> relatedOrders = orderRepository.findByTenantIdAndStoreIdAndDishIdAndCompletedFalse(currentTenantId, currentStoreId, id);
        
        if (!relatedOrders.isEmpty()) {
            throw new RuntimeException("无法删除菜品：该菜品存在未完成的订单，请先处理相关订单");
        }
        
        // 检查菜品是否属于当前租户和门店
        Optional<Dish> dish = dishRepository.findByIdAndTenantIdAndStoreId(id, currentTenantId, currentStoreId);
        if (!dish.isPresent()) {
            throw new RuntimeException("菜品不存在或无权限访问");
        }
        
        dishRepository.deleteById(id);
    }
}