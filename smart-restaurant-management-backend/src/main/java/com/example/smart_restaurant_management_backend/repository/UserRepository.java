package com.example.smart_restaurant_management_backend.repository;

import com.example.smart_restaurant_management_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.smart_restaurant_management_backend.enums.UserType;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUuid(String uuid);
    Optional<User> findByEmail(String email);
    
    // 更新：使用Long类型的tenantId和storeId
    List<User> findByUserType(UserType userType);
    List<User> findByTenantId(Long tenantId);
    
    // 新增：返回单个用户的方法
    Optional<User> findFirstByTenantId(Long tenantId);
    
    List<User> findByTenantIdAndStoreId(Long tenantId, Long storeId);
    Optional<User> findByIdAndTenantIdAndStoreId(Long id, Long tenantId, Long storeId);
    
    boolean existsByUsername(String username);
    boolean existsByUuid(String uuid);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // 删除方法
    void deleteByTenantId(Long tenantId);

    // 删除指定租户和店铺下的所有员工（只删除员工，不删除店长）
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.tenantId = :tenantId AND u.storeId = :storeId AND u.userType = 'EMPLOYEE'")
    void deleteByTenantIdAndStoreId(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);

    // 更新方法：将指定租户下所有用户的 store_id 和 current_store_id 设置为 null
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET store_id = NULL, current_store_id = NULL WHERE tenant_id = :tenantId", nativeQuery = true)
    void updateStoreIdsToNullByTenantId(@Param("tenantId") Long tenantId);

    // 更新方法：将指定店铺下所有用户的 store_id 和 current_store_id 设置为 null
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET store_id = NULL, current_store_id = NULL WHERE store_id = :storeId", nativeQuery = true)
    void updateStoreIdsToNullByStoreId(@Param("storeId") Long storeId);

    // 更新方法：将指定租户下指定店铺的非员工用户的 store_id 更新为新的店铺ID
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET store_id = :newStoreId, current_store_id = :newStoreId WHERE tenant_id = :tenantId AND store_id = :oldStoreId AND user_type != 'EMPLOYEE'", nativeQuery = true)
    void updateStoreIdForTenantUsers(@Param("tenantId") Long tenantId, @Param("oldStoreId") Long oldStoreId, @Param("newStoreId") Long newStoreId);

    // 根据租户ID和用户类型查询用户
    List<User> findByTenantIdAndUserType(Long tenantId, UserType userType);
}