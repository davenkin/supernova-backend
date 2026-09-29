package com.company.andy.support;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import java.io.IOException;

@Slf4j
@Component
@Profile("it-embedded")
public class TestingEmbeddedRedisServer {
    private static RedisServer redisServer;

    @PostConstruct
    public synchronized void startRedisServer() {
        if (redisServer != null) {
            return;
        }

        try {
            redisServer = new RedisServer(6126);
            redisServer.start();
        } catch (IOException e) {
            log.warn("Failed to start embedded Redis server.", e);
        }
    }

    @PreDestroy
    public synchronized void stopRedisServer() {
        if (redisServer == null) {
            return;
        }

        try {
            redisServer.stop();
        } catch (IOException e) {
            log.warn("Failed to stop embedded Redis server", e);
        }
    }
}
