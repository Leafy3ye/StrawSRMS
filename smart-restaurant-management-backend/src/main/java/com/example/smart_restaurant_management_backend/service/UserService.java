package com.example.smart_restaurant_management_backend.service;
import com.example.smart_restaurant_management_backend.dto.LoginRequestDTO;
import com.example.smart_restaurant_management_backend.dto.RegisterRequestDTO;
import com.example.smart_restaurant_management_backend.dto.UpdateUserDTO;
import com.example.smart_restaurant_management_backend.dto.UserDTO;
import com.example.smart_restaurant_management_backend.dto.ShopSetupDTO;
import com.example.smart_restaurant_management_backend.dto.DeleteAccountDTO;
import com.example.smart_restaurant_management_backend.dto.PasswordResetDTO;
import com.example.smart_restaurant_management_backend.dto.EmployeeRegisterDTO;
import com.example.smart_restaurant_management_backend.model.User;
import com.example.smart_restaurant_management_backend.repository.UserRepository;
import com.example.smart_restaurant_management_backend.repository.TableRepository;
import com.example.smart_restaurant_management_backend.repository.OrderRepository;
import com.example.smart_restaurant_management_backend.repository.TransactionRepository;
import com.example.smart_restaurant_management_backend.repository.DishRepository;
import com.example.smart_restaurant_management_backend.repository.MemberRepository;
import com.example.smart_restaurant_management_backend.repository.StoreRepository;
import com.example.smart_restaurant_management_backend.repository.TenantRepository;
// 添加以下两行缺失的导入
import com.example.smart_restaurant_management_backend.model.Tenant;
import com.example.smart_restaurant_management_backend.model.Store;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import com.example.smart_restaurant_management_backend.dto.RestaurantSetupRequest;
import com.example.smart_restaurant_management_backend.enums.UserType;
import com.example.smart_restaurant_management_backend.dto.OrderDetailDTO;
import com.example.smart_restaurant_management_backend.model.Order;
import com.example.smart_restaurant_management_backend.model.Tenant.TenantStatus;
import com.example.smart_restaurant_management_backend.model.Store.StoreStatus;

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

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private StoreRepository storeRepository;

    // 删除账户和所有相关数据
    @Transactional
    public boolean deleteAccount(String tenantId, DeleteAccountDTO deleteAccountDTO) {
        // 验证确认文本
        if (!"DELETE".equals(deleteAccountDTO.getConfirmText())) {
            throw new RuntimeException("确认文本不正确");
        }
        
        // 获取用户信息
        Optional<User> userOpt = userRepository.findFirstByTenantId(Long.parseLong(tenantId));
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
            Long tenantIdLong = Long.parseLong(tenantId);

            // 1. 删除订单（依赖桌位和菜品）
            orderRepository.deleteByTenantId(tenantIdLong);

            // 2. 删除交易记录
            transactionRepository.deleteByTenantId(tenantIdLong);

            // 3. 删除桌位
            tableRepository.deleteByTenantId(tenantIdLong);

            // 4. 删除菜品
            dishRepository.deleteByTenantId(tenantIdLong);

            // 5. 删除会员
            memberRepository.deleteByTenantId(tenantIdLong);

            // 6. 删除该租户下的所有用户（包括当前用户）
            userRepository.deleteByTenantId(tenantIdLong);

            // 7. 删除该租户下的所有店铺
            storeRepository.deleteByTenantId(tenantIdLong);

            // 8. 最后删除租户
            tenantRepository.deleteById(tenantIdLong);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除账户失败: " + e.getMessage());
        }
    }

    // 管理员删除租户（不需要密码验证）
    @Transactional
    public boolean deleteTenantByAdmin(Long tenantId) {
        try {
            // 检查租户是否存在
            Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
            if (!tenantOpt.isPresent()) {
                throw new RuntimeException("租户不存在");
            }

            Tenant tenant = tenantOpt.get();

            // 防止删除系统租户
            if ("SYSTEM".equals(tenant.getSubscriptionPlan()) || "系统租户".equals(tenant.getTenantName())) {
                throw new RuntimeException("不能删除系统租户");
            }

            // 按照外键依赖顺序删除数据
            // 1. 删除订单（依赖桌位和菜品）
            orderRepository.deleteByTenantId(tenantId);

            // 2. 删除交易记录
            transactionRepository.deleteByTenantId(tenantId);

            // 3. 删除桌位（引用店铺）
            tableRepository.deleteByTenantId(tenantId);

            // 4. 删除菜品（引用店铺）
            dishRepository.deleteByTenantId(tenantId);

            // 5. 删除会员（引用店铺）
            memberRepository.deleteByTenantId(tenantId);

            // 6. 先将该租户下所有用户的 store_id 和 current_store_id 设置为 null
            System.out.println("开始更新租户 " + tenantId + " 下用户的店铺引用...");
            List<User> users = userRepository.findByTenantId(tenantId);
            System.out.println("找到 " + users.size() + " 个用户需要更新");

            if (!users.isEmpty()) {
                userRepository.updateStoreIdsToNullByTenantId(tenantId);
                System.out.println("用户店铺引用更新完成");
            }

            // 7. 删除该租户下的所有店铺
            List<Store> stores = storeRepository.findByTenant_Id(tenantId);
            System.out.println("找到 " + stores.size() + " 个店铺需要删除");
            for (Store store : stores) {
                storeRepository.delete(store);
            }
            System.out.println("店铺删除完成");

            // 8. 删除该租户下的所有用户
            userRepository.deleteByTenantId(tenantId);

            // 9. 最后删除租户
            tenantRepository.deleteById(tenantId);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除租户失败: " + e.getMessage());
        }
    }

    // 初始化管理员账户
    @PostConstruct
    public void init() {
        // 检查是否已存在admin用户
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        if (!existingAdmin.isPresent()) {  // 修改：使用 !isPresent() 替代 isEmpty()
            // 创建系统租户
            Tenant systemTenant = new Tenant();
            systemTenant.setTenantName("系统租户");
            systemTenant.setEmail("system@restaurant.com");
            systemTenant.setPhone("000-0000-0000");
            systemTenant.setStatus(TenantStatus.ACTIVE);
            systemTenant.setSubscriptionPlan("SYSTEM");
            systemTenant = tenantRepository.save(systemTenant);
            
            // 检查是否已存在系统默认店铺
            Optional<Store> existingStore = storeRepository.findByTenant_IdAndIsDefaultTrue(systemTenant.getId());
            Store systemStore;
            if (!existingStore.isPresent()) {  // 修改：使用 !isPresent() 替代 isEmpty()
                // 创建系统默认店铺
                systemStore = new Store();
                systemStore.setStoreName("系统默认店铺");
                systemStore.setTenant(systemTenant);
                systemStore.setStoreAddress("系统默认地址");
                systemStore.setStorePhone("000-0000-0000");
                systemStore.setStatus(StoreStatus.ACTIVE);
                systemStore.setIsDefault(true);
                systemStore = storeRepository.save(systemStore);
            } else {
                systemStore = existingStore.get();
            }
            
            // 创建admin用户
            // 创建admin用户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("398670671@qq.com");
            admin.setPhone("000-0000-0000"); // 添加这一行
            admin.setUserType(UserType.SUPER_ADMIN);
            // 超级管理员不绑定任何租户和店铺
            admin.setTenantId(null);
            admin.setStoreId(null);
            userRepository.save(admin);
            
            System.out.println("Admin user created with username: admin, password: admin");
        }
    }

    // 用户登录
    public UserDTO login(LoginRequestDTO loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 验证密码
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // 验证用户类型
                if (loginRequest.getUserType() != null) {
                    UserType requestedType = UserType.valueOf(loginRequest.getUserType());

                    // 特殊处理：超级管理员可以通过普通登录方式登录
                    if (user.getUserType() == UserType.SUPER_ADMIN && requestedType == UserType.TENANT) {
                        return convertToDTO(user);
                    }

                    // 其他情况需要严格匹配用户类型
                    if (!user.getUserType().equals(requestedType)) {
                        throw new RuntimeException("用户类型不匹配，请选择正确的登录方式");
                    }
                }

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
    // 用户注册
    @Transactional
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
        
        // 创建租户
        Tenant tenant = new Tenant();
        tenant.setTenantName(registerRequest.getUsername() + "的餐厅");
        tenant.setEmail(registerRequest.getEmail());
        tenant.setPhone(registerRequest.getPhone());
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setSubscriptionPlan("BASIC");
        Tenant savedTenant = tenantRepository.save(tenant);
        
        // 创建默认店铺
        Store defaultStore = new Store();
        defaultStore.setStoreName("默认店铺");
        defaultStore.setTenant(savedTenant);
        defaultStore.setStoreAddress("待设置");
        defaultStore.setStorePhone(registerRequest.getPhone());
        defaultStore.setStatus(StoreStatus.ACTIVE);
        defaultStore.setIsDefault(true);
        Store savedStore = storeRepository.save(defaultStore);
        
        // 创建用户并关联租户和店铺
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setUserType(UserType.TENANT);
        user.setTenantId(savedTenant.getId());
        user.setStoreId(savedStore.getId());
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

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setTenantId(user.getTenantId());
        dto.setStoreId(user.getStoreId());
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRestaurantName(user.getRestaurantName());
        dto.setSetupCompleted(user.getSetupCompleted());
        dto.setStoreMode(user.getStoreMode());
        dto.setCurrentStoreId(user.getCurrentStoreId());
        dto.setThemeSettings(user.getThemeSettings());
        dto.setUserType(user.getUserType().name());
        dto.setCreatedAt(user.getCreatedAt()); // 添加创建时间映射

        // 如果是员工，设置店铺名称
        if (user.getUserType() == UserType.EMPLOYEE && user.getStoreId() != null) {
            Optional<Store> storeOpt = storeRepository.findById(user.getStoreId());
            if (storeOpt.isPresent()) {
                dto.setStoreName(storeOpt.get().getStoreName());
            }
        }

        return dto;
    }

    // 在UserService类的最后添加这个方法
    private boolean isValidUsername(String username) {
        // 支持中文、英文、数字和下划线，长度3-20个字符
        return username != null && username.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,20}$");
    }

    // 店铺初始化设置
    public UserDTO setupShop(String tenantId, ShopSetupDTO shopSetupDTO) {
        Optional<User> userOpt = userRepository.findFirstByTenantId(Long.parseLong(tenantId));
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 更新店铺信息
            user.setRestaurantName(shopSetupDTO.getShopName());
            user.setSetupCompleted(true);

            // 设置店铺模式
            if (shopSetupDTO.getMode() != null) {
                user.setStoreMode(shopSetupDTO.getMode());
            }

            // 保存品牌名称到租户表
            if (shopSetupDTO.getBrandName() != null && !shopSetupDTO.getBrandName().trim().isEmpty()) {
                Optional<Tenant> tenantOpt = tenantRepository.findById(Long.parseLong(tenantId));
                if (tenantOpt.isPresent()) {
                    Tenant tenant = tenantOpt.get();
                    tenant.setTenantName(shopSetupDTO.getBrandName());
                    tenantRepository.save(tenant);
                }
            }

            // 更新默认店铺的名称，并确保用户当前店铺设置为默认店铺
            if (shopSetupDTO.getShopName() != null && !shopSetupDTO.getShopName().trim().isEmpty()) {
                Optional<Store> defaultStoreOpt = storeRepository.findByTenant_IdAndIsDefaultTrue(Long.parseLong(tenantId));
                if (defaultStoreOpt.isPresent()) {
                    Store defaultStore = defaultStoreOpt.get();
                    defaultStore.setStoreName(shopSetupDTO.getShopName());
                    storeRepository.save(defaultStore);

                    // 确保用户的当前店铺设置为默认店铺
                    user.setCurrentStoreId(defaultStore.getId());
                    user.setStoreId(defaultStore.getId()); // 同时更新主店铺ID
                }
            }

            // 创建桌位
            if (shopSetupDTO.getTableCount() != null && shopSetupDTO.getTableCount() > 0) {
                createInitialTables(tenantId, shopSetupDTO.getTableCount());
            }

            // 最后保存用户信息（包含更新的店铺设置）
            User savedUser = userRepository.save(user);
            return convertToDTO(savedUser);
        }
        return null;
    }
    
    // 更新店铺名称
    public UserDTO updateShopName(String tenantId, String shopName) {
        // 修复第272行：使用 findFirstByTenantId
        Optional<User> userOpt = userRepository.findFirstByTenantId(Long.parseLong(tenantId));
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
     * 更新主题设置（基于租户ID，保留兼容性）
     * @param tenantId 租户ID
     * @param themeSettingsJson 主题设置JSON字符串
     * @return 更新后的用户DTO
     */
    public UserDTO updateThemeSettings(String tenantId, String themeSettingsJson) {
        // 修复第428行：使用 findFirstByTenantId
        Optional<User> userOpt = userRepository.findFirstByTenantId(Long.parseLong(tenantId));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setThemeSettings(themeSettingsJson);
            User savedUser = userRepository.save(user);
            return convertToDTO(savedUser);
        }
        return null;
    }

    /**
     * 更新主题设置（基于用户UUID）
     * @param userUuid 用户UUID
     * @param themeSettingsJson 主题设置JSON字符串
     * @return 更新后的用户DTO
     */
    public UserDTO updateThemeSettingsByUuid(String userUuid, String themeSettingsJson) {
        Optional<User> userOpt = userRepository.findByUuid(userUuid);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setThemeSettings(themeSettingsJson);
            User savedUser = userRepository.save(user);
            return convertToDTO(savedUser);
        }
        return null;
    }

    /**
     * 根据租户ID获取用户信息
     * @param tenantId 租户ID
     * @return 用户DTO
     */
    public UserDTO getUserByTenantId(String tenantId) {
        Optional<User> userOpt = userRepository.findFirstByTenantId(Long.parseLong(tenantId));
        return userOpt.map(this::convertToDTO).orElse(null);
    }

    // 员工注册（由店长创建）
    @Transactional
    public UserDTO registerEmployee(EmployeeRegisterDTO employeeRegisterDTO) {
        // 验证用户名、邮箱等是否已存在
        if (userRepository.existsByUsername(employeeRegisterDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(employeeRegisterDTO.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        if (userRepository.existsByPhone(employeeRegisterDTO.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        // 获取当前租户ID
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }

        // 验证店铺是否属于当前租户
        Optional<Store> storeOpt = storeRepository.findByIdAndTenant_Id(employeeRegisterDTO.getStoreId(), currentTenantId);
        if (!storeOpt.isPresent()) {
            throw new RuntimeException("店铺不存在或无权限");
        }

        // 创建员工用户
        User employee = new User();
        employee.setUsername(employeeRegisterDTO.getUsername());
        employee.setEmail(employeeRegisterDTO.getEmail());
        employee.setPhone(employeeRegisterDTO.getPhone());
        employee.setPassword(passwordEncoder.encode(employeeRegisterDTO.getPassword()));
        employee.setUserType(UserType.EMPLOYEE);
        employee.setTenantId(currentTenantId);
        employee.setStoreId(employeeRegisterDTO.getStoreId());
        employee.setCurrentStoreId(employeeRegisterDTO.getStoreId()); // 员工固定在指定店铺
        employee.setSetupCompleted(true); // 员工账号无需初始化设置

        User savedEmployee = userRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    // 获取指定租户下的所有员工
    public List<UserDTO> getEmployeesByTenantId(Long tenantId) {
        List<User> employees = userRepository.findByTenantIdAndUserType(tenantId, UserType.EMPLOYEE);
        return employees.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    // 删除员工
    @Transactional
    public void deleteEmployee(Long employeeId) {
        // 获取当前租户ID
        Long currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new RuntimeException("未找到当前租户信息");
        }

        // 验证员工是否属于当前租户
        Optional<User> employeeOpt = userRepository.findById(employeeId);
        if (!employeeOpt.isPresent()) {
            throw new RuntimeException("员工不存在");
        }

        User employee = employeeOpt.get();
        if (!employee.getTenantId().equals(currentTenantId)) {
            throw new RuntimeException("无权限删除该员工");
        }

        if (!employee.getUserType().equals(UserType.EMPLOYEE)) {
            throw new RuntimeException("只能删除员工账号");
        }

        userRepository.delete(employee);
    }

    // 更新用户个人信息
    public UserDTO updateUserProfile(String userUuid, Map<String, Object> updateData) {
        Optional<User> userOpt = userRepository.findByUuid(userUuid);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();

        // 更新用户名
        if (updateData.containsKey("username")) {
            String newUsername = (String) updateData.get("username");
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                user.setUsername(newUsername.trim());
            }
        }

        // 更新头像URL
        if (updateData.containsKey("avatarUrl")) {
            String avatarUrl = (String) updateData.get("avatarUrl");
            user.setAvatarUrl(avatarUrl);
        }

        // 更新密码（如果提供了当前密码和新密码）
        if (updateData.containsKey("currentPassword") && updateData.containsKey("newPassword")) {
            String currentPassword = (String) updateData.get("currentPassword");
            String newPassword = (String) updateData.get("newPassword");

            if (currentPassword != null && newPassword != null) {
                // 验证当前密码
                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    throw new RuntimeException("当前密码不正确");
                }

                // 设置新密码
                user.setPassword(passwordEncoder.encode(newPassword));
            }
        }

        // 保存更新
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }
}