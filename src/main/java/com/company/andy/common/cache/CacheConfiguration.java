package com.company.andy.common.cache;

import com.company.andy.feature.equipment.domain.CachedOrgEquipmentSummaries;
import com.company.andy.feature.systemsettings.domain.SystemSettings;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import static com.company.andy.common.utils.Constants.*;
import static java.time.Duration.ofDays;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

// All caches should be registered here using withCacheConfiguration(), as the cacheDefaults() might not work for some objects' serialization/deserialization
// Also the withCacheConfiguration() serves as a documentation for all caches in the system, as you can easily find all cache names and their configurations here

@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(ObjectMapper objectMapper) {
        return builder -> builder
                .cacheDefaults(defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new GenericJacksonJsonRedisSerializer(objectMapper)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(ORG_EQUIPMENTS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, CachedOrgEquipmentSummaries.class)))
                        .entryTtl(ofDays(30)))
                .withCacheConfiguration(SYSTEM_SETTINGS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(objectMapper, SystemSettings.class)))
                        .entryTtl(ofDays(30)))
                ;
    }
}
