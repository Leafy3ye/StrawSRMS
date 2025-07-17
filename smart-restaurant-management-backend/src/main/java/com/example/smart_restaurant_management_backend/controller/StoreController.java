package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.model.Store;
import com.example.smart_restaurant_management_backend.model.Tenant;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.service.StoreService;
import com.example.smart_restaurant_management_backend.repository.TenantRepository;
import com.example.smart_restaurant_management_backend.repository.UserRepository;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {

    @Autowired
    private StoreService storeService;
    
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取当前租户的所有店铺
     */
    @GetMapping
    public ResponseEntity<?> getAllStores() {
        try {
            List<Store> stores = storeService.getAllStores();
            return ResponseEntity.ok(stores);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取店铺列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取店铺详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStoreById(@PathVariable Long id) {
        try {
            Optional<Store> store = storeService.getStoreById(id);
            if (store.isPresent()) {
                return ResponseEntity.ok(store.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取店铺详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建新店铺
     */
    @PostMapping
    public ResponseEntity<?> createStore(@RequestBody Map<String, Object> request) {
        try {
            Long currentTenantId = TenantContext.getCurrentTenantId();
            if (currentTenantId == null) {
                return ResponseEntity.badRequest().body("未找到当前租户信息");
            }

            // 获取租户信息
            Optional<Tenant> tenantOpt = tenantRepository.findById(currentTenantId);
            if (!tenantOpt.isPresent()) {
                return ResponseEntity.badRequest().body("租户不存在");
            }

            // 创建店铺对象
            Store store = new Store();
            store.setStoreName((String) request.get("storeName"));
            store.setAddress((String) request.get("address"));
            store.setPhone((String) request.get("phone"));
            store.setTenant(tenantOpt.get());
            store.setIsDefault(false); // 新创建的店铺默认不是主店
            store.setStatus(Store.StoreStatus.ACTIVE);

            Store savedStore = storeService.saveStore(store);
            return ResponseEntity.ok(savedStore);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建店铺失败: " + e.getMessage());
        }
    }

    /**
     * 更新店铺信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStore(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Optional<Store> storeOpt = storeService.getStoreById(id);
            if (!storeOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Store store = storeOpt.get();
            
            // 更新店铺信息
            if (request.containsKey("storeName")) {
                store.setStoreName((String) request.get("storeName"));
            }
            if (request.containsKey("address")) {
                store.setAddress((String) request.get("address"));
            }
            if (request.containsKey("phone")) {
                store.setPhone((String) request.get("phone"));
            }
            if (request.containsKey("status")) {
                String status = (String) request.get("status");
                store.setStatus(Store.StoreStatus.valueOf(status));
            }

            Store updatedStore = storeService.saveStore(store);
            return ResponseEntity.ok(updatedStore);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新店铺失败: " + e.getMessage());
        }
    }

    /**
     * 删除店铺
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStore(@PathVariable Long id) {
        try {
            Optional<Store> storeOpt = storeService.getStoreById(id);
            if (!storeOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Store store = storeOpt.get();
            
            // 不允许删除默认店铺
            if (store.getIsDefault()) {
                return ResponseEntity.badRequest().body("不能删除默认店铺");
            }

            storeService.deleteStore(id);
            return ResponseEntity.ok("店铺删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除店铺失败: " + e.getMessage());
        }
    }

    /**
     * 切换到指定店铺
     */
    @PostMapping("/{id}/switch")
    public ResponseEntity<?> switchToStore(@PathVariable Long id) {
        try {
            Long currentTenantId = TenantContext.getCurrentTenantId();
            if (currentTenantId == null) {
                return ResponseEntity.badRequest().body("未找到当前租户信息");
            }

            Optional<Store> storeOpt = storeService.getStoreById(id);
            if (!storeOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // 更新用户的当前店铺ID
            Optional<User> userOpt = userRepository.findFirstByTenantId(currentTenantId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setCurrentStoreId(id);
                userRepository.save(user);
            }

            // 设置当前店铺上下文
            TenantContext.setCurrentStoreId(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "切换店铺成功");
            response.put("storeId", id);
            response.put("storeName", storeOpt.get().getStoreName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("切换店铺失败: " + e.getMessage());
        }
    }
}
