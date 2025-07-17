package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.model.Store;
import com.example.smart_restaurant_management_backend.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {
    
    @Autowired
    private StoreRepository storeRepository;
    
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
    
    public void deleteStore(Long id) {
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }
        Optional<Store> store = storeRepository.findByIdAndTenant_Id(id, currentTenantId);
        if (store.isPresent()) {
            storeRepository.delete(store.get());
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