package com.example.coffee_management_system_backend.repository;

import com.example.coffee_management_system_backend.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
}
