package com.dynamiclog.log4j2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 验证 {@link Log4j2LoggingSystemAdapter} 基于 Log4j2 原生 API 的运行时级别调整能力。
 * <p>
 * log4j-core 为该模块的 optional/compile 依赖，测试运行期可用。
 */
class Log4j2LoggingSystemAdapterTest {

    private final Log4j2LoggingSystemAdapter adapter = new Log4j2LoggingSystemAdapter();

    @Test
    void nameIsLog4j2() {
        assertEquals("log4j2", adapter.getName());
    }

    @Test
    void setThenGetLogLevelReflectsChange() {
        adapter.setLogLevel("com.foo.test", "DEBUG");
        assertEquals("DEBUG", adapter.getLogLevel("com.foo.test"));
    }

    @Test
    void setLogLevelIsCaseInsensitive() {
        adapter.setLogLevel("com.foo.test.ci", "warn");
        assertEquals("WARN", adapter.getLogLevel("com.foo.test.ci"));
    }

    @Test
    void resetLogLevelDoesNotThrow() {
        adapter.setLogLevel("com.foo.test.reset", "DEBUG");
        assertDoesNotThrow(() -> adapter.resetLogLevel("com.foo.test.reset"));
    }

    @Test
    void setNullLevelResetsWithoutThrowing() {
        adapter.setLogLevel("com.foo.test.null", "DEBUG");
        assertDoesNotThrow(() -> adapter.setLogLevel("com.foo.test.null", null));
    }

    @Test
    void invalidLevelStringIsSkippedWithoutThrowing() {
        assertDoesNotThrow(() -> adapter.setLogLevel("com.foo.test.invalid", "NOT_A_LEVEL"));
    }

    @Test
    void getLoggerNamesReturnsNonNullCollection() {
        assertNotNull(adapter.getLoggerNames());
    }
}
