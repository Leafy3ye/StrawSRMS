package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Store;
import com.example.smart_restaurant_management_backend.model.Store.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    // 根据租户ID查询所有店铺
    List<Store> findByTenant_Id(Long tenantId);
    
    // 根据租户ID和店铺状态查询店铺
    List<Store> findByTenant_IdAndStatus(Long tenantId, StoreStatus status);
    
    // 根据租户ID查询默认店铺
    Optional<Store> findByTenant_IdAndIsDefaultTrue(Long tenantId);
    
    // 根据ID和租户ID查询店铺
    Optional<Store> findByIdAndTenant_Id(Long id, Long tenantId);

    Optional<Store> findByStoreNameAndTenant_Id(String storeName, Long tenantId);
    
    // 检查店铺名称是否已存在（排除指定ID）
    boolean existsByTenant_IdAndStoreNameAndIdNot(Long tenantId, String storeName, Long id);
    
    // 检查店铺名称是否已存在
    boolean existsByTenant_IdAndStoreName(Long tenantId, String storeName);
    
    // 统计租户下的店铺数量
    @Query("SELECT COUNT(s) FROM Store s WHERE s.tenant.id = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);
    
    // 统计租户下活跃店铺数量
    @Query("SELECT COUNT(s) FROM Store s WHERE s.tenant.id = :tenantId AND s.status = :status")
    Long countByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") StoreStatus status);
    
    // 删除操作
    @Modifying
    @Transactional
    @Query("DELETE FROM Store s WHERE s.tenant.id = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);
}