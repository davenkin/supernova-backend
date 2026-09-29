package com.company.andy.support.testid;

public final class TestIdContext {
    private static final ThreadLocal<String> CURRENT_TEST_ID = new ThreadLocal<>();

    private TestIdContext() {
    }

    public static void setTestId(String testId) {
        CURRENT_TEST_ID.set(testId);
    }

    public static String getTestId() {
        return CURRENT_TEST_ID.get();
    }

    public static void clear() {
        CURRENT_TEST_ID.remove();
    }
}
