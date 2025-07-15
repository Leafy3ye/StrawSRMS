package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    // 根据邮箱查找租户
    Optional<Tenant> findByEmail(String email);
    
    // 根据租户名称查找租户
    Optional<Tenant> findByTenantName(String tenantName);
    
    // 检查邮箱是否已存在
    boolean existsByEmail(String email);
    
    // 检查租户名称是否已存在
    boolean existsByTenantName(String tenantName);
    
    // 根据租户名称和邮箱查找（排除指定ID）
    boolean existsByTenantNameAndIdNot(String tenantName, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
}