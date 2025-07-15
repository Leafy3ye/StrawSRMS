package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.RechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RechargeRecordRepository extends JpaRepository<RechargeRecord, Long> {
    
    // 根据会员ID、租户ID和店铺ID查询充值记录（按创建时间倒序）
    List<RechargeRecord> findByMemberIdAndTenantIdAndStoreIdOrderByCreatedAtDesc(Long memberId, Long tenantId, Long storeId);
    
    // 根据会员ID和租户ID查询充值记录（跨店铺，按创建时间倒序）
    List<RechargeRecord> findByMemberIdAndTenantIdOrderByCreatedAtDesc(Long memberId, Long tenantId);
    
    // 根据租户ID和店铺ID查询所有充值记录
    List<RechargeRecord> findByTenantIdAndStoreId(Long tenantId, Long storeId);
    
    // 根据租户ID查询所有充值记录（跨店铺）
    List<RechargeRecord> findByTenantId(Long tenantId);
    
    // 根据会员ID查询充值记录（按创建时间倒序）- 兼容旧方法
    List<RechargeRecord> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    
    // 根据ID、租户ID和店铺ID查询单条记录
    Optional<RechargeRecord> findByIdAndTenantIdAndStoreId(Long id, Long tenantId, Long storeId);
    
    // 根据ID和租户ID查询单条记录（跨店铺）
    Optional<RechargeRecord> findByIdAndTenantId(Long id, Long tenantId);
    
    // 根据时间范围查询充值记录 - 店铺级别
    List<RechargeRecord> findByTenantIdAndStoreIdAndCreatedAtBetween(
        Long tenantId, Long storeId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 根据时间范围查询充值记录 - 租户级别
    List<RechargeRecord> findByTenantIdAndCreatedAtBetween(
        Long tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 统计查询 - 店铺级别今日充值总额
    @Query("SELECT COALESCE(SUM(rr.amount), 0.0) FROM RechargeRecord rr WHERE rr.tenantId = :tenantId AND rr.storeId = :storeId AND DATE(rr.createdAt) = CURRENT_DATE")
    java.math.BigDecimal sumTodayRechargeByTenantIdAndStoreId(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);
    
    // 统计查询 - 租户级别今日充值总额
    @Query("SELECT COALESCE(SUM(rr.amount), 0.0) FROM RechargeRecord rr WHERE rr.tenantId = :tenantId AND DATE(rr.createdAt) = CURRENT_DATE")
    java.math.BigDecimal sumTodayRechargeByTenantId(@Param("tenantId") Long tenantId);
    
    // 统计查询 - 店铺级别今日充值次数
    @Query("SELECT COUNT(rr) FROM RechargeRecord rr WHERE rr.tenantId = :tenantId AND rr.storeId = :storeId AND DATE(rr.createdAt) = CURRENT_DATE")
    Long countTodayRechargeByTenantIdAndStoreId(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);
    
    // 统计查询 - 租户级别今日充值次数
    @Query("SELECT COUNT(rr) FROM RechargeRecord rr WHERE rr.tenantId = :tenantId AND DATE(rr.createdAt) = CURRENT_DATE")
    Long countTodayRechargeByTenantId(@Param("tenantId") Long tenantId);
    
    // 删除操作
    @Modifying
    @Transactional
    @Query("DELETE FROM RechargeRecord rr WHERE rr.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RechargeRecord rr WHERE rr.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RechargeRecord rr WHERE rr.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}