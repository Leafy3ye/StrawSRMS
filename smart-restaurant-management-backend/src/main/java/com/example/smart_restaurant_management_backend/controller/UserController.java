package com.example.smart_restaurant_management_backend.controller;

import com.example.smart_restaurant_management_backend.dto.LoginRequestDTO;
import com.example.smart_restaurant_management_backend.dto.RegisterRequestDTO;
import com.example.smart_restaurant_management_backend.dto.UpdateUserDTO;
import com.example.smart_restaurant_management_backend.dto.UserDTO;
import com.example.smart_restaurant_management_backend.service.UserService;
import com.example.smart_restaurant_management_backend.service.EmailService;
import com.example.smart_restaurant_management_backend.service.CaptchaService;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import com.example.smart_restaurant_management_backend.util.JwtUtil;

// 添加 Spring Web 相关的 import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            UserDTO user = userService.login(loginRequest);
            if (user != null) {
                // 生成 JWT Token
                List<String> roles = Arrays.asList("USER"); // 根据实际角色设置
                String token = jwtUtil.generateToken(
                    user.getUsername(), 
                    user.getTenantId(), 
                    roles
                );
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", user);
                response.put("message", "登录成功");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "用户名或密码错误");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "登录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

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
}