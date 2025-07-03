package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Integer> {
    
    // 使用原生SQL插入桌位
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tables (name, status, created_at, updated_at) VALUES (:name, :status, NOW(), NOW())", nativeQuery = true)
    void insertTableWithSql(@Param("name") String name, @Param("status") String status);
    
    // 使用原生SQL更新桌位
    @Modifying
    @Transactional
    @Query(value = "UPDATE tables SET name = :name, status = :status, updated_at = NOW() WHERE id = :id", nativeQuery = true)
    void updateTableWithSql(@Param("id") Integer id, @Param("name") String name, @Param("status") String status);
    
    // 使用原生SQL删除桌位
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tables WHERE id = :id", nativeQuery = true)
    void deleteTableWithSql(@Param("id") Integer id);
}
