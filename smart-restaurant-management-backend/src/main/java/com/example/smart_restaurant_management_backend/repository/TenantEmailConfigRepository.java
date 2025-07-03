package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.TenantEmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantEmailConfigRepository extends JpaRepository<TenantEmailConfig, Long> {
    TenantEmailConfig findByTenantId(Long tenantId);
    void deleteByTenantId(Long tenantId);
}