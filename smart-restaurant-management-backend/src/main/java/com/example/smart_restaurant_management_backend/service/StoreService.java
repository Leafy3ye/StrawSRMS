package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.model.Store;
import com.example.smart_restaurant_management_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;
    
    public List<Store> getAllStores() {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return storeRepository.findByTenant_Id(currentTenantId);
    }
    
    public Optional<Store> getStoreById(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        return storeRepository.findByIdAndTenant_Id(id, currentTenantId);
    }
    
    public Store saveStore(Store store) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }

        // 如果是新店铺且没有设置租户，则设置当前租户
        if (store.getTenant() == null || store.getTenant().getId() == null) {
            // 这里应该从数据库获取租户对象，而不是直接设置ID
            // 在Controller中已经设置了完整的Tenant对象，所以这里不需要额外处理
        }

        return storeRepository.save(store);
    }
    
    @Transactional
    public void deleteStore(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }

        Optional<Store> storeOpt = storeRepository.findByIdAndTenant_Id(id, currentTenantId);
        if (!storeOpt.isPresent()) {
            throw new RuntimeException("店铺不存在或无权限访问");
        }

        Store store = storeOpt.get();

        // 不允许删除默认店铺
        if (store.getIsDefault()) {
            throw new RuntimeException("不能删除默认店铺");
        }

        try {
            // 按照外键依赖顺序删除该店铺的所有关联数据

            // 1. 删除订单（依赖桌位和菜品）
            orderRepository.deleteByTenantIdAndStoreId(currentTenantId, id);

            // 2. 删除交易记录
            transactionRepository.deleteByTenantIdAndStoreId(currentTenantId, id);

            // 3. 删除桌位
            tableRepository.deleteByTenantIdAndStoreId(currentTenantId, id);

            // 4. 删除菜品
            dishRepository.deleteByTenantIdAndStoreId(currentTenantId, id);

            // 5. 删除会员
            memberRepository.deleteByTenantIdAndStoreId(currentTenantId, id);

            // 6. 删除该店铺下的所有员工账号（只删除员工，不删除店长）
            userRepository.deleteByTenantIdAndStoreId(currentTenantId, id);

            // 7. 将该店铺下的店长账号的 store_id 更新为默认店铺
            Optional<Store> defaultStoreOpt = storeRepository.findByTenant_IdAndIsDefaultTrue(currentTenantId);
            if (defaultStoreOpt.isPresent()) {
                Long defaultStoreId = defaultStoreOpt.get().getId();
                userRepository.updateStoreIdForTenantUsers(currentTenantId, id, defaultStoreId);
            } else {
                // 如果没有默认店铺，将店长的 store_id 设置为 null
                userRepository.updateStoreIdsToNullByStoreId(id);
            }

            // 8. 最后删除店铺
            storeRepository.delete(store);

        } catch (Exception e) {
            throw new RuntimeException("删除店铺失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查店铺是否属于指定租户
     * @param storeId 店铺ID
     * @param tenantId 租户ID
     * @return 是否属于该租户
     */
    public boolean isStoreOwnedByTenant(Long storeId, Long tenantId) {
        if (storeId == null || tenantId == null) {
            return false;
        }
        return storeRepository.findByIdAndTenant_Id(storeId, tenantId).isPresent();
    }
    
    /**
     * 根据租户ID获取默认店铺ID
     * @param tenantId 租户ID
     * @return 默认店铺ID，如果没有找到则返回null
     */
    public Long getDefaultStoreIdByTenantId(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        
        List<Store> stores = storeRepository.findByTenant_Id(tenantId);
        if (stores.isEmpty()) {
            return null;
        }
        
        // 返回第一个店铺作为默认店铺
        // 如果有特定的默认店铺逻辑，可以在这里实现
        return stores.get(0).getId();
    }
}