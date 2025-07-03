package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
}
