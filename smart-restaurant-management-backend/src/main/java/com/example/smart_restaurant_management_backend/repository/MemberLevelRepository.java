package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.MemberLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberLevelRepository extends JpaRepository<MemberLevel, Long> {
    
    // 根据租户ID查询所有等级（按排序权重排序）
    List<MemberLevel> findByTenantIdAndIsEnabledTrueOrderBySortOrder(String tenantId);
    
    // 根据租户ID和等级名称查询
    Optional<MemberLevel> findByTenantIdAndLevelName(String tenantId, String levelName);
    
    // 根据租户ID和ID查询
    Optional<MemberLevel> findByIdAndTenantId(Long id, String tenantId);
    
    // 检查等级名称是否已存在（排除指定ID）
    boolean existsByTenantIdAndLevelNameAndIdNot(String tenantId, String levelName, Long id);
    
    // 检查等级名称是否已存在
    boolean existsByTenantIdAndLevelName(String tenantId, String levelName);
}