package com.example.coffee_management_system_backend.repository;

import com.example.coffee_management_system_backend.model.RechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RechargeRecordRepository extends JpaRepository<RechargeRecord, Long> {
    
    List<RechargeRecord> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}