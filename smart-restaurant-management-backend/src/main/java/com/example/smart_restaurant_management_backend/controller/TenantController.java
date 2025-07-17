package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.Tenant;
import com.example.smart_restaurant_management_backend.repository.TenantRepository;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = "*")
public class TenantController {

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * 获取当前租户信息
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentTenant() {
        try {
            Long currentTenantId = TenantContext.getCurrentTenantId();
            if (currentTenantId == null) {
                return ResponseEntity.badRequest().body("未找到当前租户信息");
            }

            Optional<Tenant> tenantOpt = tenantRepository.findById(currentTenantId);
            if (!tenantOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(tenantOpt.get());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取租户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前租户信息
     */
    @PutMapping("/current")
    public ResponseEntity<?> updateCurrentTenant(@RequestBody Map<String, Object> updateData) {
        try {
            Long currentTenantId = TenantContext.getCurrentTenantId();
            if (currentTenantId == null) {
                return ResponseEntity.badRequest().body("未找到当前租户信息");
            }

            Optional<Tenant> tenantOpt = tenantRepository.findById(currentTenantId);
            if (!tenantOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Tenant tenant = tenantOpt.get();
            
            // 更新租户名称
            if (updateData.containsKey("tenantName")) {
                tenant.setTenantName((String) updateData.get("tenantName"));
            }

            tenantRepository.save(tenant);
            return ResponseEntity.ok(tenant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新租户信息失败: " + e.getMessage());
        }
    }
}
