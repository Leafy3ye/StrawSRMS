package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.dto.*;
import com.example.smart_restaurant_management_backend.service.UserService;
import com.example.smart_restaurant_management_backend.service.CaptchaService;
import com.example.smart_restaurant_management_backend.service.EmailService;
import com.example.smart_restaurant_management_backend.util.JwtUtil;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.smart_restaurant_management_backend.dto.PasswordResetDTO;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper; // 添加这行导入
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        UserDTO user = userService.login(loginRequest);
        if (user != null) {
            // 生成JWT token - 使用UUID作为subject而不是username
            List<String> roles = Arrays.asList("USER"); // 或者根据用户类型设置角色
            // 修复：处理tenantId为null的情况
            String tenantIdStr = user.getTenantId() != null ? user.getTenantId().toString() : "0";
            // 关键修改：使用UUID而不是username作为JWT的subject
            String token = jwtUtil.generateToken(user.getUuid(), tenantIdStr, roles);
            
            // 构建包含token和user的响应
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }

    // 获取用户信息
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserDTO updateUserDTO) {
        UserDTO updatedUser = userService.updateUser(id, updateUserDTO);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.badRequest().body("更新用户信息失败");
        }
    }

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequest) {
        try {
            UserDTO user = userService.register(registerRequest);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 添加通过UUID获取用户的端点（之前缺失的）
    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<?> getUserByUuid(@PathVariable String uuid) {
        UserDTO user = userService.getUserByUuid(uuid);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 获取图形验证码
    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        try {
            CaptchaService.CaptchaResult captcha = captchaService.generateCaptcha();
            return ResponseEntity.ok(captcha);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("生成验证码失败");
        }
    }

    // 发送邮件验证码
    @PostMapping("/send-email-code")
    public ResponseEntity<?> sendEmailCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("邮箱不能为空");
            }
            
            // 检查邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return ResponseEntity.badRequest().body("邮箱格式不正确");
            }
            
            emailService.sendEmailCode(email);
            return ResponseEntity.ok("验证码已发送");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发送验证码失败：" + e.getMessage());
        }
    }

    // 店铺初始化设置
    @PostMapping("/shop-setup")
    public ResponseEntity<?> setupShop(@RequestBody ShopSetupDTO shopSetupDTO) {
        try {
            Long currentTenantId = TenantContext.getCurrentTenantId();  // 修复：使用getCurrentTenantId()替代getCurrentTenantUuid()
            if (currentTenantId == null) {
                return ResponseEntity.badRequest().body("未找到当前租户信息");
            }
            
            UserDTO updatedUser = userService.setupShop(currentTenantId.toString(), shopSetupDTO);  // 转换为String
            if (updatedUser != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("user", updatedUser);
                response.put("message", "店铺设置完成");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("店铺设置失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("店铺设置失败：" + e.getMessage());
        }
    }
    
    // 更新店铺信息
    @PutMapping("/shop-info")
    public ResponseEntity<?> updateShopInfo(@RequestBody Map<String, String> request) {
        try {
            Long currentTenantId = TenantContext.getCurrentTenantId();  // 修复：使用getCurrentTenantId()替代getCurrentTenantUuid()
            if (currentTenantId == null) {
                return ResponseEntity.badRequest().body("未找到当前租户信息");
            }
            
            String shopName = request.get("shopName");
            UserDTO updatedUser = userService.updateShopName(currentTenantId.toString(), shopName);  // 转换为String
            if (updatedUser != null) {
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.badRequest().body("更新店铺信息失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新店铺信息失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestBody DeleteAccountDTO deleteAccountDTO, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String tenantId = jwtUtil.extractTenantId(token);
                
                boolean success = userService.deleteAccount(tenantId, deleteAccountDTO);
                if (success) {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "账户删除成功");
                    return ResponseEntity.ok().body(response);
                } else {
                    return ResponseEntity.badRequest().body("删除失败，请检查输入信息");
                }
            }
            return ResponseEntity.badRequest().body("无效的token");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 验证图形验证码
    @PostMapping("/verify-captcha")
    public ResponseEntity<?> verifyCaptcha(@RequestBody Map<String, String> request) {
        try {
            String captchaKey = request.get("captchaKey");
            String captcha = request.get("captcha");
            
            if (captchaKey == null || captcha == null) {
                return ResponseEntity.badRequest().body("验证码信息不完整");
            }
            
            boolean isValid = captchaService.verifyCaptcha(captchaKey, captcha);
            if (isValid) {
                return ResponseEntity.ok("验证码正确");
            } else {
                return ResponseEntity.badRequest().body("验证码错误");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("验证码验证失败：" + e.getMessage());
        }
    }

    // 发送重置密码验证码
    @PostMapping("/send-reset-password-code")
    public ResponseEntity<?> sendResetPasswordCode(@RequestBody Map<String, String> request) {
        try {
            String account = request.get("account");
            String captchaKey = request.get("captchaKey");
            String captcha = request.get("captcha");
            
            if (account == null || account.isEmpty()) {
                return ResponseEntity.badRequest().body("账户不能为空");
            }
            
            // 验证图形验证码
            if (!captchaService.verifyCaptcha(captchaKey, captcha)) {
                return ResponseEntity.badRequest().body("图形验证码错误");
            }
            
            // 发送重置密码验证码
            userService.sendResetPasswordCode(account);
            return ResponseEntity.ok("验证码已发送到您的邮箱");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发送验证码失败：" + e.getMessage());
        }
    }

    // 重置密码
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String account = request.get("account");
            String emailCode = request.get("emailCode");
            String newPassword = request.get("newPassword");
            
            if (account == null || emailCode == null || newPassword == null) {
                return ResponseEntity.badRequest().body("信息不完整");
            }
            
            // 创建PasswordResetDTO对象
            PasswordResetDTO passwordResetDTO = new PasswordResetDTO();
            passwordResetDTO.setAccount(account);
            passwordResetDTO.setEmailCode(emailCode);
            passwordResetDTO.setNewPassword(newPassword);
            
            // UserService的resetPassword方法返回void，如果执行成功不会抛出异常
            userService.resetPassword(passwordResetDTO);
            return ResponseEntity.ok("密码重置成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("密码重置失败：" + e.getMessage());
        }
    }

    // 获取主题设置
    @GetMapping("/theme-settings")
    public ResponseEntity<?> getThemeSettings() {
        try {
            Long currentTenantId = TenantContext.getCurrentTenantId();  // 修复：使用getCurrentTenantId()替代getCurrentTenantUuid()
            if (currentTenantId == null) {
                return ResponseEntity.badRequest().body("未找到当前租户信息");
            }
            
            UserDTO user = userService.getUserByTenantId(currentTenantId.toString());  // 转换为String
            if (user != null) {
                // 返回主题设置，如果为空则返回空对象
                String themeSettings = user.getThemeSettings();
                if (themeSettings == null || themeSettings.isEmpty()) {
                    return ResponseEntity.ok(new HashMap<>());
                }
                
                // 解析JSON字符串并返回
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> themeMap = objectMapper.readValue(themeSettings, Map.class);
                return ResponseEntity.ok(themeMap);
            } else {
                return ResponseEntity.badRequest().body("用户不存在");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取主题设置失败: " + e.getMessage());
        }
    }

    // 更新主题设置
    @PutMapping("/theme-settings")
    public ResponseEntity<?> updateThemeSettings(@RequestBody Map<String, String> request) {
        try {
            Long currentTenantId = TenantContext.getCurrentTenantId();  // 修复：使用getCurrentTenantId()替代getCurrentTenantUuid()
            if (currentTenantId == null) {
                return ResponseEntity.badRequest().body("未找到当前租户信息");
            }
            
            // 将主题设置转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String themeSettingsJson = objectMapper.writeValueAsString(request);
            
            UserDTO updatedUser = userService.updateThemeSettings(currentTenantId.toString(), themeSettingsJson);  // 转换为String
            
            if (updatedUser != null) {
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.badRequest().body("更新主题设置失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新主题设置失败: " + e.getMessage());
        }
    }
}