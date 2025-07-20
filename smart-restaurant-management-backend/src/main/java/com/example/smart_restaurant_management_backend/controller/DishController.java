package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.Dish;
import com.example.smart_restaurant_management_backend.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dishes")
@CrossOrigin(origins = "*")
public class DishController {

    @Autowired
    private DishService dishService;

    // 获取所有菜品
    @GetMapping
    public List<Dish> getAllDishes() {
        return dishService.getAllDishes();
    }

    // 根据ID获取菜品
    @GetMapping("/{id}")
    public Optional<Dish> getDishById(@PathVariable Long id) {
        return dishService.getDishById(id);
    }

    // 添加菜品
    @PostMapping
    public Dish addDish(@RequestBody Dish dish) {
        return dishService.addDish(dish);
    }

    // 编辑菜品
    @PutMapping("/{id}")
    public Dish updateDish(@PathVariable Long id, @RequestBody Dish dish) {
        return dishService.updateDish(id, dish);
    }

    // 删除菜品
    @DeleteMapping("/{id}")
    public void deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
    }
}
