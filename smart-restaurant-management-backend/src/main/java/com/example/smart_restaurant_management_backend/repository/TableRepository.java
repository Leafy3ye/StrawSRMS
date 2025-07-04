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
public interface TableRepository extends JpaRepository<TableEntity, Integer> {
    
    // 基于租户的查询方法
    List<TableEntity> findByTenantId(String tenantId);
    Optional<TableEntity> findByIdAndTenantId(Integer id, String tenantId);
    
    // 使用原生SQL插入桌位 - 添加tenant_id
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tables (tenant_id, name, status, created_at, updated_at) VALUES (:tenantId, :name, :status, NOW(), NOW())", nativeQuery = true)
    void insertTableWithSql(@Param("tenantId") String tenantId, @Param("name") String name, @Param("status") String status);
    
    // 使用原生SQL更新桌位
    @Modifying
    @Transactional
    @Query(value = "UPDATE tables SET name = :name, status = :status, updated_at = NOW() WHERE id = :id AND tenant_id = :tenantId", nativeQuery = true)
    void updateTableWithSql(@Param("id") Integer id, @Param("tenantId") String tenantId, @Param("name") String name, @Param("status") String status);
    
    // 使用原生SQL删除桌位
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tables WHERE id = :id AND tenant_id = :tenantId", nativeQuery = true)
    void deleteTableWithSql(@Param("id") Integer id, @Param("tenantId") String tenantId);
}
