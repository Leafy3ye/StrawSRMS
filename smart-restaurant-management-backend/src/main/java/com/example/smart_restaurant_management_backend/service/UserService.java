package com.example.smart_restaurant_management_backend.service;
import com.example.smart_restaurant_management_backend.dto.LoginRequestDTO;
import com.example.smart_restaurant_management_backend.dto.RegisterRequestDTO;
import com.example.smart_restaurant_management_backend.dto.UpdateUserDTO;
import com.example.smart_restaurant_management_backend.dto.UserDTO;
import com.example.smart_restaurant_management_backend.dto.ShopSetupDTO;
import com.example.smart_restaurant_management_backend.dto.DeleteAccountDTO;
import com.example.smart_restaurant_management_backend.dto.PasswordResetDTO;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.repository.UserRepository;
import com.example.smart_restaurant_management_backend.repository.TableRepository;
import com.example.smart_restaurant_management_backend.repository.OrderRepository;
import com.example.smart_restaurant_management_backend.repository.TransactionRepository;
import com.example.smart_restaurant_management_backend.repository.DishRepository;
import com.example.smart_restaurant_management_backend.repository.MemberRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;
import com.example.smart_restaurant_management_backend.dto.RestaurantSetupRequest;
import com.example.smart_restaurant_management_backend.model.UserType;
import com.example.smart_restaurant_management_backend.dto.OrderDetailDTO;
import com.example.smart_restaurant_management_backend.model.Order;

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

    @Autowired
    private TableService tableService;

    @Autowired
    private TableRepository tableRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private DishRepository dishRepository;
    
    @Autowired
    private MemberRepository memberRepository;

    // 删除账户和所有相关数据
    @Transactional
    public boolean deleteAccount(String tenantId, DeleteAccountDTO deleteAccountDTO) {
        // 验证确认文本
        if (!"DELETE".equals(deleteAccountDTO.getConfirmText())) {
            throw new RuntimeException("确认文本不正确");
        }
        
        // 获取用户信息
        Optional<User> userOpt = userRepository.findByTenantId(tenantId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 验证当前密码
        if (!passwordEncoder.matches(deleteAccountDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("当前密码不正确");
        }
        
        try {
            // 按照外键依赖顺序删除数据
            
            // 1. 删除订单（依赖桌位和菜品）
            orderRepository.deleteByTenantId(tenantId);
            
            // 2. 删除交易记录
            transactionRepository.deleteByTenantId(tenantId);
            
            // 3. 删除桌位
            tableRepository.deleteByTenantId(tenantId);
            
            // 4. 删除菜品
            dishRepository.deleteByTenantId(tenantId);
            
            // 5. 删除会员
            memberRepository.deleteByTenantId(tenantId);
            
            // 6. 最后删除用户
            userRepository.delete(user);
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除账户失败: " + e.getMessage());
        }
    }

    // 初始化管理员账户
    @PostConstruct
    public void init() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("398670671@xx.com");
            admin.setPhone("13800000000");
            admin.setUserType(UserType.SUPER_ADMIN);  // ✅ 设置为超级管理员
            admin.setEmailVerified(true);             // ✅ 设置邮箱已验证
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

        // 验证用户名、邮箱等是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setUserType(UserType.TENANT);
        user.setSetupCompleted(false);
        // uuid会在@PrePersist中自动生成
        
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }
    
    public UserDTO completeSetup(String uuid, RestaurantSetupRequest request) {
        User user = userRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
            
        user.setRestaurantName(request.getRestaurantName());
        user.setRestaurantAddress(request.getRestaurantAddress());
        user.setRestaurantPhone(request.getRestaurantPhone());
        user.setSetupCompleted(true);
        
        User savedUser = userRepository.save(user);
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
        dto.setTenantId(user.getTenantId());
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRestaurantName(user.getRestaurantName());
        dto.setSetupCompleted(user.getSetupCompleted());
        dto.setThemeSettings(user.getThemeSettings()); // 添加这一行
        return dto;
    }

    // 在UserService类的最后添加这个方法
    private boolean isValidUsername(String username) {
        // 支持中文、英文、数字和下划线，长度3-20个字符
        return username != null && username.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,20}$");
    }

    // 店铺初始化设置
    public UserDTO setupShop(String tenantId, ShopSetupDTO shopSetupDTO) {
        Optional<User> userOpt = userRepository.findByTenantId(tenantId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // 更新店铺信息
            user.setRestaurantName(shopSetupDTO.getShopName());
            user.setSetupCompleted(true);
            
            // 保存用户信息
            User savedUser = userRepository.save(user);
            
            // 创建桌位
            if (shopSetupDTO.getTableCount() != null && shopSetupDTO.getTableCount() > 0) {
                createInitialTables(tenantId, shopSetupDTO.getTableCount());
            }
            
            return convertToDTO(savedUser);
        }
        return null;
    }
    
    // 更新店铺名称
    public UserDTO updateShopName(String tenantId, String shopName) {
        Optional<User> userOpt = userRepository.findByTenantId(tenantId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRestaurantName(shopName);
            User savedUser = userRepository.save(user);
            return convertToDTO(savedUser);
        }
        return null;
    }
    
    // 创建初始桌位
    private void createInitialTables(String tenantId, Integer tableCount) {
        for (int i = 1; i <= tableCount; i++) {
            try {
                tableService.saveWithSql("桌位 " + i, "未开桌");
            } catch (Exception e) {
                // 记录错误但不中断流程
                System.err.println("创建桌位失败: " + e.getMessage());
            }
        }
    }

    /**
     * 发送重置密码验证码
     * @param account 账户（用户名或邮箱）
     * @throws Exception 发送失败时抛出异常
     */
    public void sendResetPasswordCode(String account) throws Exception {
        if (account == null || account.trim().isEmpty()) {
            throw new RuntimeException("账户不能为空");
        }
        
        // 查找用户（支持用户名或邮箱）
        User user = null;
        if (isValidEmail(account)) {
            // 如果是邮箱格式，按邮箱查找
            Optional<User> userOpt = userRepository.findByEmail(account);
            if (userOpt.isPresent()) {
                user = userOpt.get();
            }
        } else {
            // 否则按用户名查找
            Optional<User> userOpt = userRepository.findByUsername(account);
            if (userOpt.isPresent()) {
                user = userOpt.get();
            }
        }
        
        if (user == null) {
            throw new RuntimeException("账户不存在");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("该账户未绑定邮箱，无法发送重置密码验证码");
        }
        
        // 发送验证码到用户绑定的邮箱
        try {
            emailService.sendResetPasswordCode(user.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("发送重置密码验证码失败：" + e.getMessage());
        }
    }

    /**
     * 重置密码
     * @param passwordResetDTO 密码重置DTO
     * @throws Exception 重置失败时抛出异常
     */
    @Transactional
    public void resetPassword(PasswordResetDTO passwordResetDTO) throws Exception {
        if (passwordResetDTO.getAccount() == null || passwordResetDTO.getAccount().trim().isEmpty()) {
            throw new RuntimeException("账户不能为空");
        }
        
        if (passwordResetDTO.getEmailCode() == null || passwordResetDTO.getEmailCode().trim().isEmpty()) {
            throw new RuntimeException("邮箱验证码不能为空");
        }
        
        if (passwordResetDTO.getNewPassword() == null || passwordResetDTO.getNewPassword().trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        
        // 查找用户（支持用户名或邮箱）
        User user = null;
        String emailForVerification = null;
        
        if (isValidEmail(passwordResetDTO.getAccount())) {
            // 如果是邮箱格式，按邮箱查找
            Optional<User> userOpt = userRepository.findByEmail(passwordResetDTO.getAccount());
            if (userOpt.isPresent()) {
                user = userOpt.get();
                emailForVerification = passwordResetDTO.getAccount();
            }
        } else {
            // 否则按用户名查找
            Optional<User> userOpt = userRepository.findByUsername(passwordResetDTO.getAccount());
            if (userOpt.isPresent()) {
                user = userOpt.get();
                emailForVerification = user.getEmail();
            }
        }
        
        if (user == null) {
            throw new RuntimeException("账户不存在");
        }
        
        if (emailForVerification == null || emailForVerification.trim().isEmpty()) {
            throw new RuntimeException("该账户未绑定邮箱，无法重置密码");
        }
        
        // 验证邮箱验证码
        // 验证邮箱验证码
        if (!emailService.verifyResetPasswordCode(emailForVerification, passwordResetDTO.getEmailCode())) {
            throw new RuntimeException("邮箱验证码错误或已过期");
        }
        
        // 更新密码
        try {
            user.setPassword(passwordEncoder.encode(passwordResetDTO.getNewPassword()));
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("重置密码失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新主题设置
     * @param tenantId 租户ID
     * @param themeSettingsJson 主题设置JSON字符串
     * @return 更新后的用户DTO
     */
    public UserDTO updateThemeSettings(String tenantId, String themeSettingsJson) {
        Optional<User> userOpt = userRepository.findByTenantId(tenantId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setThemeSettings(themeSettingsJson);
            User savedUser = userRepository.save(user);
            return convertToDTO(savedUser);
        }
        return null;
    }
}