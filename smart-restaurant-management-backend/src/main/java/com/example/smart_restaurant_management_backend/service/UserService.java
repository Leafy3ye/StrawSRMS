package com.example.smart_restaurant_management_backend.service;
import com.example.smart_restaurant_management_backend.dto.LoginRequestDTO;
import com.example.smart_restaurant_management_backend.dto.RegisterRequestDTO;  // 添加这行import
import com.example.smart_restaurant_management_backend.dto.UpdateUserDTO;
import com.example.smart_restaurant_management_backend.dto.UserDTO;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaptchaService captchaService;

    // 初始化管理员账户
    @PostConstruct
    public void init() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            // UUID会在@PrePersist中自动生成
            userRepository.save(admin);
        }
    }

    // 用户登录
    public UserDTO login(LoginRequestDTO loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return convertToDTO(user);
            }
        }
        
        return null; // 登录失败
    }

    // 通过ID获取用户信息
    public UserDTO getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(this::convertToDTO).orElse(null);
    }

    // 通过UUID获取用户信息 - 新增方法
    public UserDTO getUserByUuid(String uuid) {
        Optional<User> userOpt = userRepository.findByUuid(uuid);
        return userOpt.map(this::convertToDTO).orElse(null);
    }

    // 更新用户信息
    public UserDTO updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // 验证当前密码
            if (updateUserDTO.getCurrentPassword() != null && 
                !passwordEncoder.matches(updateUserDTO.getCurrentPassword(), user.getPassword())) {
                return null; // 当前密码不正确
            }
            
            // 更新用户名
            if (updateUserDTO.getUsername() != null && !updateUserDTO.getUsername().isEmpty()) {
                // 检查用户名是否已存在
                if (!user.getUsername().equals(updateUserDTO.getUsername()) && 
                    userRepository.existsByUsername(updateUserDTO.getUsername())) {
                    return null; // 用户名已存在
                }
                user.setUsername(updateUserDTO.getUsername());
            }
            
            // 更新密码
            if (updateUserDTO.getNewPassword() != null && !updateUserDTO.getNewPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updateUserDTO.getNewPassword()));
            }
            
            // 更新头像
            if (updateUserDTO.getAvatarUrl() != null) {
                user.setAvatarUrl(updateUserDTO.getAvatarUrl());
            }
            
            userRepository.save(user);
            return convertToDTO(user);
        }
        
        return null; // 用户不存在
    }

    // 用户注册
    public UserDTO register(RegisterRequestDTO registerRequest) {
        // 验证图形验证码
        if (!captchaService.verifyCaptcha(registerRequest.getCaptchaKey(), registerRequest.getCaptcha())) {
            throw new RuntimeException("图形验证码错误");
        }

        // 验证邮件验证码
        if (!emailService.verifyEmailCode(registerRequest.getEmail(), registerRequest.getEmailCode())) {
            throw new RuntimeException("邮件验证码错误或已过期");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (userRepository.existsByPhone(registerRequest.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }
        
        // 验证用户名格式
        if (!isValidUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名格式不正确，只能包含字母、数字和下划线，长度3-20个字符");
        }
        
        // 验证密码长度
        if (registerRequest.getPassword().length() < 6 || registerRequest.getPassword().length() > 20) {
            throw new RuntimeException("密码长度必须在6-20个字符之间");
        }

        // 验证邮箱格式
        if (!isValidEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱格式不正确");
        }

        // 验证手机号格式
        if (!isValidPhone(registerRequest.getPhone())) {
            throw new RuntimeException("手机号格式不正确");
        }
        
        // 创建新用户
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPhone(registerRequest.getPhone());
        newUser.setEmailVerified(true); // 通过邮件验证码验证后设为已验证
        
        User savedUser = userRepository.save(newUser);
        return convertToDTO(savedUser);
    }

    // 验证邮箱格式
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // 验证手机号格式
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    // 保留这个完整的convertToDTO方法
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    // 在UserService类的最后添加这个方法
    private boolean isValidUsername(String username) {
        // 支持中文、英文、数字和下划线，长度3-20个字符
        return username != null && username.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,20}$");
    }
}