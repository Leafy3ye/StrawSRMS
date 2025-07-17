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
    // 更新：使用Long类型的tenantId和storeId
    List<Member> findByTenantIdAndStoreId(Long tenantId, Long storeId);
    Optional<Member> findByIdAndTenantIdAndStoreId(Long id, Long tenantId, Long storeId);
    List<Member> findByTenantIdAndStoreIdAndNameContaining(Long tenantId, Long storeId, String keyword);
    Optional<Member> findByTenantIdAndStoreIdAndPhone(Long tenantId, Long storeId, String phone);
    Optional<Member> findByTenantIdAndStoreIdAndEmail(Long tenantId, Long storeId, String email);

    // 添加只按租户ID查询的方法（用于超级管理员查看租户所有会员）
    List<Member> findByTenantId(Long tenantId);
    Optional<Member> findByIdAndTenantId(Long id, Long tenantId);
    List<Member> findByTenantIdAndNameContaining(Long tenantId, String keyword);

    @Modifying
    @Transactional
    @Query("DELETE FROM Member m WHERE m.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Member m WHERE m.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Member m WHERE m.tenantId = :tenantId AND m.storeId = :storeId")
    void deleteByTenantIdAndStoreId(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);
}