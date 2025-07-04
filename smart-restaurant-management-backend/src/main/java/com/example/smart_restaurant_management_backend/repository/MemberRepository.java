package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 根据租户ID查询会员
    List<Member> findByTenantId(String tenantId);
    
    // 根据租户ID和会员ID查询
    Optional<Member> findByIdAndTenantId(Long id, String tenantId);
    
    // 根据租户ID和姓名模糊查询
    List<Member> findByTenantIdAndNameContaining(String tenantId, String keyword);
    
    // 根据租户ID和手机号查询
    Optional<Member> findByTenantIdAndPhone(String tenantId, String phone);
    
    // 根据租户ID和邮箱查询
    Optional<Member> findByTenantIdAndEmail(String tenantId, String email);
    
    // 兼容旧方法
    List<Member> findByNameContaining(String keyword);

    @Modifying
    @Transactional
    @Query("DELETE FROM Member m WHERE m.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") String tenantId);
}