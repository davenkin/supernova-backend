package com.company.andy.common.ratelimiter;

import com.company.andy.common.configuration.property.CommonProperties;
import com.company.andy.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.company.andy.common.exception.ErrorCode.TOO_MANY_REQUEST;
import static com.company.andy.common.utils.CommonUtils.requireNonBlank;
import static com.company.andy.common.utils.NullableMapUtils.mapOf;
import static java.lang.Integer.parseInt;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {
    private final static int WINDOW_SECONDS = 5;
    private final StringRedisTemplate stringRedisTemplate;
    private final CommonProperties commonProperties;

    @Override
    public void applyFor(String orgId, String key, int tps) {
        requireNonBlank(orgId, "Org ID must not be blank.");
        requireNonBlank(key, "Key must not be blank.");

        doApply(key + ":" + orgId + ":" + now().getEpochSecond() / WINDOW_SECONDS, tps * WINDOW_SECONDS);
    }

    @Override
    public void applyFor(String key, int tps) {
        requireNonBlank(key, "Key must not be blank.");

        doApply(key + ":" + now().getEpochSecond() / WINDOW_SECONDS, tps * WINDOW_SECONDS);
    }

    private void doApply(String key, int limit) {
        if (!commonProperties.limitRate()) {
            return;
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than 1.");
        }

        String finalKey = "RateLimit:" + key;
        String count = stringRedisTemplate.opsForValue().get(finalKey);
        if (isNotBlank(count) && parseInt(count) >= limit) {
            throw new ServiceException(TOO_MANY_REQUEST, "Too many request.", mapOf("key", finalKey));
        }

        stringRedisTemplate.opsForValue().increment(finalKey);
        stringRedisTemplate.expire(finalKey, WINDOW_SECONDS + 10, SECONDS);
    }
}
