package com.example.smart_restaurant_management_backend.service;

import com.example.smart_restaurant_management_backend.dto.LoginRequestDTO;
import com.example.smart_restaurant_management_backend.dto.UpdateUserDTO;
import com.example.smart_restaurant_management_backend.dto.UserDTO;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 初始化管理员账户
    @PostConstruct
    public void init() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
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

    // 获取用户信息
    public UserDTO getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
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

    // 将User实体转换为UserDTO
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }
}