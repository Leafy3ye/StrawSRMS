package com.example.smart_restaurant_management_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class SmartRestaurantManagementBackendApplication {

    // 创建一个日志记录器
    private static final Logger logger = LoggerFactory.getLogger(SmartRestaurantManagementBackendApplication.class);

    public static void main(String[] args) {
        // 记录应用启动日志
        logger.info("Starting Coffee Management System Backend application...");

        try {
            // 启动 Spring Boot 应用
            SpringApplication.run(SmartRestaurantManagementBackendApplication.class, args);
            // 记录应用成功启动的日志
            logger.info("Application started successfully!");
        } catch (Exception e) {
            // 捕获异常并记录日志
            logger.error("Application startup failed!", e);
        }
    }
}
