package com.example.smart_restaurant_management_backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.time.Duration;

@Configuration
@EnableCaching
public class ConditionalCacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ConditionalCacheConfig.class);
    
    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;
    
    /**
     * 检查Redis是否可用
     */
    private boolean isRedisAvailable(RedisConnectionFactory redisConnectionFactory) {
        if (redisConnectionFactory == null) {
            return false;
        }
        try {
            redisConnectionFactory.getConnection().ping();
            logger.info("✅ Redis连接测试成功");
            return true;
        } catch (Exception e) {
            logger.warn("❌ Redis连接失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 主缓存管理器 - 优先使用Redis，失败时降级为内存缓存
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        // 检查Redis是否可用
        if (redisConnectionFactory != null && isRedisAvailable(redisConnectionFactory)) {
            logger.info("🚀 Redis可用，创建Redis缓存管理器");
            return createRedisCacheManager(redisConnectionFactory);
        } else {
            logger.warn("⚠️ Redis不可用，使用内存缓存管理器，验证码等服务暂不可用！");
            return createMemoryCacheManager();
        }
    }
    
    /**
     * 创建Redis缓存管理器
     */
    private CacheManager createRedisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .disableCachingNullValues())
                .build();
    }
    
    /**
     * 创建内存缓存管理器
     */
    private CacheManager createMemoryCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList("dishes", "members", "memberBalance"));
        return cacheManager;
    }
    
    /**
     * RedisTemplate配置 - 仅在Redis可用时创建
     */
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        if (!isRedisAvailable(redisConnectionFactory)) {
            logger.info("Redis不可用，跳过RedisTemplate配置");
            return null;
        }
        
        try {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory);
            
            StringRedisSerializer stringSerializer = new StringRedisSerializer();
            GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
            
            template.setKeySerializer(stringSerializer);
            template.setHashKeySerializer(stringSerializer);
            template.setValueSerializer(jsonSerializer);
            template.setHashValueSerializer(jsonSerializer);
            
            template.afterPropertiesSet();
            logger.info("✅ RedisTemplate配置成功");
            return template;
        } catch (Exception e) {
            logger.warn("❌ RedisTemplate配置失败: {}", e.getMessage());
            return null;
        }
    }
}