package com.example.coffee_management_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class CoffeeManagementSystemBackendApplication {

    // 创建一个日志记录器
    private static final Logger logger = LoggerFactory.getLogger(CoffeeManagementSystemBackendApplication.class);

    public static void main(String[] args) {
        // 记录应用启动日志
        logger.info("Starting Coffee Management System Backend application...");

        try {
            // 启动 Spring Boot 应用
            SpringApplication.run(CoffeeManagementSystemBackendApplication.class, args);
            // 记录应用成功启动的日志
            logger.info("Application started successfully!");
        } catch (Exception e) {
            // 捕获异常并记录日志
            logger.error("Application startup failed!", e);
        }
    }
}
