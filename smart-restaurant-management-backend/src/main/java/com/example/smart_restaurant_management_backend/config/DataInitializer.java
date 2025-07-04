package com.example.smart_restaurant_management_backend.config;

import com.example.smart_restaurant_management_backend.model.*;
import com.example.smart_restaurant_management_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // 检查数据库是否为空
        if (userRepository.count() == 0) {
            System.out.println("检测到空数据库，数据库结构已创建完成。");
            System.out.println("您可以手动创建用户来测试多租户功能。");
        } else {
            System.out.println("数据库已存在数据，系统启动完成。");
        }
    }
}