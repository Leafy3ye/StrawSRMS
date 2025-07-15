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
                return new LongValue(-1); // 默认值，确保查询不到数据
            }
            
            @Override
            public String getTenantIdColumn() {
                return "tenant_id"; // 租户字段名
            }
            
            @Override
            public boolean ignoreTable(String tableName) {
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