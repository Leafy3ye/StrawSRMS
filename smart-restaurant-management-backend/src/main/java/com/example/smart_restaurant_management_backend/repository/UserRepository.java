package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUuid(String uuid);
    Optional<User> findByEmail(String email);
    Optional<User> findByTenantId(String tenantId);
    boolean existsByUsername(String username);
    boolean existsByUuid(String uuid);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}