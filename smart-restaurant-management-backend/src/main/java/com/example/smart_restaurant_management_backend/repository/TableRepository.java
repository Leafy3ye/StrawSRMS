package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long> {
    
    // 更新：使用Long类型的tenantId和storeId
    List<TableEntity> findByTenantIdAndStoreId(Long tenantId, Long storeId);
    Optional<TableEntity> findByIdAndTenantIdAndStoreId(Long id, Long tenantId, Long storeId);
    
    // 原生SQL查询 - 更新参数类型
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tables (tenant_id, store_id, name, status, created_at, updated_at) VALUES (:tenantId, :storeId, :name, :status, NOW(), NOW())", nativeQuery = true)
    void insertTableWithSql(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId, @Param("name") String name, @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE tables SET name = :name, status = :status, updated_at = NOW() WHERE id = :id AND tenant_id = :tenantId AND store_id = :storeId", nativeQuery = true)
    void updateTableWithSql(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("storeId") Long storeId, @Param("name") String name, @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tables WHERE id = :id AND tenant_id = :tenantId AND store_id = :storeId", nativeQuery = true)
    void deleteTableWithSql(@Param("id") Integer id, @Param("tenantId") Long tenantId, @Param("storeId") Long storeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TableEntity t WHERE t.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TableEntity t WHERE t.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TableEntity t WHERE t.tenantId = :tenantId AND t.storeId = :storeId")
    void deleteByTenantIdAndStoreId(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);
}
