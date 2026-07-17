package com.dynamiclog.ttl;

import com.dynamiclog.core.adapter.DefaultLoggingSystemAdapterRegistry;
import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.event.DefaultLogEventBus;
import com.dynamiclog.core.manager.DynamicLogManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporaryLogLevelManagerTest {

    private MockAdapter adapter;
    private TemporaryLogLevelManager ttlManager;

    @BeforeEach
    void setUp() {
        DefaultLoggingSystemAdapterRegistry registry = new DefaultLoggingSystemAdapterRegistry();
        adapter = new MockAdapter("logback");
        registry.register(adapter);
        DynamicLogManager manager = new DynamicLogManager(registry, new DefaultLogEventBus());
        ttlManager = new TemporaryLogLevelManager(manager);
    }

    @AfterEach
    void tearDown() {
        ttlManager.shutdown();
    }

    @Test
    void appliesThenRollsBackToOriginalLevel() throws InterruptedException {
        adapter.setLogLevel("com.example", "INFO");

        ttlManager.applyTemporary("com.example", "DEBUG", 100);
        assertEquals("DEBUG", adapter.getLogLevel("com.example"), "应用后应为临时级别");

        Thread.sleep(400);
        assertEquals("INFO", adapter.getLogLevel("com.example"), "到期后应回滚为原级别");
    }

    @Test
    void rollsBackToInheritWhenNoOriginalLevel() throws InterruptedException {
        // 原本未显式设置（继承父级）
        ttlManager.applyTemporary("com.inherit", "DEBUG", 100);
        assertEquals("DEBUG", adapter.getLogLevel("com.inherit"));

        Thread.sleep(400);
        assertNull(adapter.getLogLevel("com.inherit"), "原级别为 null 时到期应重置为继承");
        assertTrue(adapter.wasReset("com.inherit"), "应调用 resetLogLevel");
    }

    @Test
    void repeatedApplyCancelsOldRollbackAndKeepsEarliestOriginal() throws InterruptedException {
        adapter.setLogLevel("com.example", "WARN");

        // 第一次：原级别 WARN，TTL 较短
        ttlManager.applyTemporary("com.example", "DEBUG", 200);
        // 第二次：在第一次到期前重新应用为 TRACE，TTL 更长；应取消旧回滚、保留最初原值 WARN
        Thread.sleep(80);
        ttlManager.applyTemporary("com.example", "TRACE", 400);
        assertEquals("TRACE", adapter.getLogLevel("com.example"));

        // 越过第一次的 TTL：旧回滚若未取消会把级别错误改回 WARN
        Thread.sleep(220);
        assertEquals("TRACE", adapter.getLogLevel("com.example"), "旧回滚应已被取消，级别保持新临时值");

        // 越过第二次的 TTL：回滚应恢复到最初记录的 WARN，而非中间临时值
        Thread.sleep(400);
        assertEquals("WARN", adapter.getLogLevel("com.example"), "应回滚到最初原级别 WARN");
    }

    @Test
    void cancelRollsBackImmediately() {
        adapter.setLogLevel("com.example", "INFO");

        ttlManager.applyTemporary("com.example", "DEBUG", 10_000);
        assertEquals("DEBUG", adapter.getLogLevel("com.example"));

        ttlManager.cancel("com.example");
        assertEquals("INFO", adapter.getLogLevel("com.example"), "cancel 应立即回滚");
    }

    /**
     * 简单的内存 mock 适配器：区分「显式设置为某级别」「重置为继承（null）」两种状态。
     */
    static class MockAdapter implements LoggingSystemAdapter {
        private final String name;
        private final Map<String, String> levels = new HashMap<>();
        private final Map<String, Boolean> reset = new HashMap<>();

        MockAdapter(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public synchronized void setLogLevel(String loggerName, String level) {
            levels.put(loggerName, level);
            reset.remove(loggerName);
        }

        @Override
        public synchronized String getLogLevel(String loggerName) {
            return levels.get(loggerName);
        }

        @Override
        public synchronized Collection<String> getLoggerNames() {
            return levels.keySet();
        }

        @Override
        public synchronized void resetLogLevel(String loggerName) {
            levels.remove(loggerName);
            reset.put(loggerName, Boolean.TRUE);
        }

        synchronized boolean wasReset(String loggerName) {
            return reset.getOrDefault(loggerName, Boolean.FALSE);
        }
    }
}
