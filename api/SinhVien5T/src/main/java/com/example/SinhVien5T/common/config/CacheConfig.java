package com.example.SinhVien5T.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Configuration
public class CacheConfig implements CachingConfigurer {

    public static final String USER_PROFILE = "user:profile";
    public static final String USER_AUTH = "user:auth";
    public static final String PERMISSION_USER = "permission:user";
    public static final String CAMPAIGN_CURRENT = "campaign:current";
    public static final String CAMPAIGN_DETAIL = "campaign:detail";
    public static final String CAMPAIGN_CRITERIA = "campaign:criteria";
    public static final String CAMPAIGN_CRITERIA_USER = "campaign:criteria:user";
    public static final String APPLICATION_RECORD_MINE = "application-record:mine";
    public static final String DASHBOARD_ADMIN = "dashboard:admin";
    public static final String DASHBOARD_CAMPAIGN = "dashboard:campaign";
    public static final String NOTIFICATION_UNREAD = "notification:unread";

    @Value("${app.redis.key-prefix:sv5t}")
    private String keyPrefix;

    @Value("${app.redis.cache.enabled:true}")
    private boolean redisCacheEnabled;

    @Bean
    public CacheManager cacheManager(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory connectionFactory
    ) {
        if (!redisCacheEnabled) {
            return new ConcurrentMapCacheManager(
                    USER_PROFILE,
                    USER_AUTH,
                    PERMISSION_USER,
                    CAMPAIGN_CURRENT,
                    CAMPAIGN_DETAIL,
                    CAMPAIGN_CRITERIA,
                    CAMPAIGN_CRITERIA_USER,
                    APPLICATION_RECORD_MINE,
                    DASHBOARD_ADMIN,
                    DASHBOARD_CAMPAIGN,
                    NOTIFICATION_UNREAD
            );
        }

        var jsonPair = RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer());

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> keyPrefix + ":" + cacheName + "::")
                .serializeValuesWith(jsonPair);

        Map<String, RedisCacheConfiguration> configs = Map.ofEntries(
                Map.entry(USER_PROFILE, defaults.entryTtl(Duration.ofMinutes(15))),
                Map.entry(USER_AUTH, defaults.entryTtl(Duration.ofMinutes(10))),
                Map.entry(PERMISSION_USER, defaults.entryTtl(Duration.ofMinutes(10))),
                Map.entry(CAMPAIGN_CURRENT, defaults.entryTtl(Duration.ofMinutes(5))),
                Map.entry(CAMPAIGN_DETAIL, defaults.entryTtl(Duration.ofMinutes(15))),
                Map.entry(CAMPAIGN_CRITERIA, defaults.entryTtl(Duration.ofMinutes(30))),
                Map.entry(CAMPAIGN_CRITERIA_USER, defaults.entryTtl(Duration.ofMinutes(3))),
                Map.entry(APPLICATION_RECORD_MINE, defaults.entryTtl(Duration.ofMinutes(3))),
                Map.entry(DASHBOARD_ADMIN, defaults.entryTtl(Duration.ofSeconds(60))),
                Map.entry(DASHBOARD_CAMPAIGN, defaults.entryTtl(Duration.ofSeconds(60))),
                Map.entry(NOTIFICATION_UNREAD, defaults.entryTtl(Duration.ofSeconds(30)))
        );

        return RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory))
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(configs)
                .transactionAware()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache get failed: cache={}, key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis cache put failed: cache={}, key={}", cache.getName(), key, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache evict failed: cache={}", cache.getName(), exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis cache clear failed: cache={}", cache.getName(), exception);
            }
        };
    }
}
