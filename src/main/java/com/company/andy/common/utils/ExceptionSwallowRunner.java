package com.company.andy.common.utils;

import lombok.extern.slf4j.Slf4j;

// Mainly used by EventHandlers where multiple steps should be executed independently inside a single handler
@Slf4j
public class ExceptionSwallowRunner {

    public static void run(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            log.error("Failed to run: ", ex);
        }
    }
}
