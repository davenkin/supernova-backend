package com.company.andy.common.infrastructure.retry;


import com.company.andy.IntegrationTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class RetryableIntegrationTest extends IntegrationTest {

    @Autowired
    private TestingRetryService testingRetryService;

    @Test
    public void retryable_should_work() {
        String record = RandomStringUtils.secure().nextAlphanumeric(10);
        assertFalse(testingRetryService.getRetryRecords().contains(record));

        assertThrows(RuntimeException.class, () -> {
            testingRetryService.retry(record);
        });

        assertEquals(3, testingRetryService.getRetryRecords().size());
        assertEquals(record, testingRetryService.getRetryRecords().get(0));
        assertEquals(record, testingRetryService.getRetryRecords().get(1));
        assertEquals(record, testingRetryService.getRetryRecords().get(2));
    }
}
