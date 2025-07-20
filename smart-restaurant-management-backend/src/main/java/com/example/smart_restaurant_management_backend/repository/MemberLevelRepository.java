package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.MemberLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberLevelRepository extends JpaRepository<MemberLevel, Long> {
    
    // 根据租户ID和店铺ID查询所有等级（包括禁用状态，按排序权重排序）
    List<MemberLevel> findByTenantIdAndStoreIdOrderBySortOrder(Long tenantId, Long storeId);
    
    // 根据租户ID查询所有等级（跨店铺查询）
    List<MemberLevel> findByTenantIdAndIsEnabledTrueOrderBySortOrder(Long tenantId);
    
    // 根据租户ID、店铺ID和等级名称查询
    Optional<MemberLevel> findByTenantIdAndStoreIdAndLevelName(Long tenantId, Long storeId, String levelName);
    
    // 根据租户ID和等级名称查询（跨店铺）
    Optional<MemberLevel> findByTenantIdAndLevelName(Long tenantId, String levelName);
    
    // 根据租户ID、店铺ID和ID查询
    Optional<MemberLevel> findByIdAndTenantIdAndStoreId(Long id, Long tenantId, Long storeId);
    
    // 根据租户ID和ID查询（跨店铺）
    Optional<MemberLevel> findByIdAndTenantId(Long id, Long tenantId);
    
    // 检查等级名称是否已存在（排除指定ID）- 店铺级别
    boolean existsByTenantIdAndStoreIdAndLevelNameAndIdNot(Long tenantId, Long storeId, String levelName, Long id);
    
    // 检查等级名称是否已存在 - 店铺级别
    boolean existsByTenantIdAndStoreIdAndLevelName(Long tenantId, Long storeId, String levelName);
    
    // 检查等级名称是否已存在（排除指定ID）- 租户级别
    boolean existsByTenantIdAndLevelNameAndIdNot(Long tenantId, String levelName, Long id);
    
    // 检查等级名称是否已存在 - 租户级别
    boolean existsByTenantIdAndLevelName(Long tenantId, String levelName);
    
    // 删除操作
    @Modifying
    @Transactional
    @Query("DELETE FROM MemberLevel ml WHERE ml.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM MemberLevel ml WHERE ml.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}