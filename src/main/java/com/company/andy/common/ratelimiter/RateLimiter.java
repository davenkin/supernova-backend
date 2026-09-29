package com.company.andy.common.ratelimiter;

public interface RateLimiter {
    void applyFor(String orgId, String key, int tps);

    void applyFor(String key, int tps);
}
