package com.company.andy.common.infrastructure.retry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@RequiredArgsConstructor
public class TestingRetryService {
    private final List<String> retryRecords = new ArrayList<>();

    @Retryable(multiplier = 1, delay = 100, maxRetries = 2)
    public void retry(String record) {
        this.retryRecords.add(record);
        throw new RuntimeException("fake retry exhausted");
    }
}
