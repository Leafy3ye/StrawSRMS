package com.example.smart_restaurant_management_backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.example.smart_restaurant_management_backend.context.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 租户拦截器
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = TenantContext.getCurrentTenantId();
                if (tenantId != null) {
                    return new LongValue(tenantId);
                }

                // 检查是否为超级管理员
                Boolean isSuperAdmin = TenantContext.getIsSuperAdmin();
                if (Boolean.TRUE.equals(isSuperAdmin)) {
                    // 超级管理员没有指定租户ID时，返回一个特殊值来跳过租户过滤
                    return new LongValue(0); // 使用0作为特殊值，表示查看所有数据
                } else {
                    // 普通用户：必须有租户ID
                    return new LongValue(-1); // 默认值，确保查询不到数据
                }
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id"; // 租户字段名
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 检查是否为超级管理员且没有指定租户ID
                Boolean isSuperAdmin = TenantContext.getIsSuperAdmin();
                Long tenantId = TenantContext.getCurrentTenantId();

                if (Boolean.TRUE.equals(isSuperAdmin) && tenantId == null) {
                    // 超级管理员查看所有数据时，忽略所有表的租户过滤
                    return true;
                }

                // 忽略不需要租户隔离的表
                return "tenants".equals(tableName) ||
                       "system_config".equals(tableName) ||
                       "flyway_schema_history".equals(tableName);
            }
        });
        
        interceptor.addInnerInterceptor(tenantInterceptor);
        
        // 分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        return interceptor;
    }
}