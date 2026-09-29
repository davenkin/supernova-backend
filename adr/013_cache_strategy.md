# Cache strategy

## Context

Cache should be used to enhance performance.

## Decision

We implement cache on `Repository` layer. The caller should know whether to use cache methods or not, namely cache is
not transparent to
callers.

## Implementation

- Use `@Cacheable` to populate cache
- Use `@CacheEvict` or [CacheEvictor](../src/main/java/com/company/andy/common/cache/CacheEvictor.java) to evict cache
- Use `CacheManager` to test cache

Example of using `@Cacheable` and `@CacheEvict`:

```java
@Repository
@RequiredArgsConstructor
public class SystemSettingsRepository extends AbstractMongoRepository<SystemSettings> {
   
    @Cacheable(value = SYSTEM_SETTINGS_CACHE, key = "'SYSTEM_SETTINGS'")
    public SystemSettings cachedSystemSettings() {
        return super.byIdOptional(SYSTEM_SETTINGS_ID).orElse(null);
    }

    @Override
    @CacheEvict(value = SYSTEM_SETTINGS_CACHE, allEntries = true)
    public void save(SystemSettings systemSettings) {
        super.save(systemSettings);
    }
}
```
- [CacheEvictor](../src/main/java/com/company/andy/common/cache/CacheEvictor.java) can be used instead of `@CacheEvict` for evicting cache if more control is needed:

```java
    public void evictCachedEquipmentSummaries(String orgId) {
        this.cacheEvictor.evict(ORG_EQUIPMENTS_CACHE, orgId);
    }
```

In order to implement a cache object, go through the following steps:

1. Decide if an exising class should be cached or a new class should be created
2. If a new class is needed, it's often created with Java's `Record`, for example:

```java
public record CachedOrgEquipmentSummaries(List<EquipmentSummary> summaries) {
}
```
3. Make sure the cached objects can be serialized/deserialized by Jackson

4. Register the cache class
   into [CacheConfiguration](../src/main/java/com/company/andy/common/configuration/CacheConfiguration.java) using
   `withCacheConfiguration()`:

```java
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
```

5. In the Repository, create standalone methods for retrieving the cache:
```java
    @Cacheable(value = SYSTEM_SETTINGS_CACHE, key = "'SYSTEM_SETTINGS'")
    public SystemSettings cachedSystemSettings() {
        return super.byIdOptional(SYSTEM_SETTINGS_ID).orElse(null);
    }
```

6. When retrieving the cache, call the cached method:
```java
    public QSystemSettings getSystemSettings() {
        SystemSettings systemSettings = systemSettingsRepository.cachedSystemSettings();
        return QSystemSettings.builder()
                .baseSettings(systemSettings.getBaseSettings())
                .build();
    }
```

7. Evict the cache when necessary, for example, in the `save()` method of the Repository:
```java
    @Override
    @CacheEvict(value = SYSTEM_SETTINGS_CACHE, allEntries = true)
    public void save(SystemSettings systemSettings) {
        super.save(systemSettings);
    }
```