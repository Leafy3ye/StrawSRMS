package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.RechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RechargeRecordRepository extends JpaRepository<RechargeRecord, Long> {
    List<RechargeRecord> findByMemberIdAndTenantIdOrderByCreatedAtDesc(Long memberId, String tenantId);
    List<RechargeRecord> findByTenantId(String tenantId);
    List<RechargeRecord> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}