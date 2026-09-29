package com.company.andy.support.testid;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.UUID;

// Injects a random testId into every test method and make it globally accessible in the testing thread,
// testId can be used to track the test execution and support verification for stub object
public class TestIdExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        TestIdContext.setTestId(UUID.randomUUID().toString());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TestIdContext.clear();
    }
}