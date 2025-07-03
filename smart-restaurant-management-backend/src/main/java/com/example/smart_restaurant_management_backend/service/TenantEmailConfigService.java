package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.model.TenantEmailConfig;
import com.example.smart_restaurant_management_backend.repository.TenantEmailConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TenantEmailConfigService {
    
    @Autowired
    private TenantEmailConfigRepository repository;
    
    @Cacheable(value = "tenantEmailConfig", key = "#tenantId")
    public TenantEmailConfig getTenantEmailConfig(Long tenantId) {
        return repository.findByTenantId(tenantId);
    }
    
    @CacheEvict(value = "tenantEmailConfig", key = "#config.tenantId")
    public TenantEmailConfig saveTenantEmailConfig(TenantEmailConfig config) {
        return repository.save(config);
    }
    
    @CacheEvict(value = "tenantEmailConfig", key = "#tenantId")
    public void deleteTenantEmailConfig(Long tenantId) {
        repository.deleteByTenantId(tenantId);
    }
}