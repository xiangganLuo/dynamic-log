package com.dynamiclog.apollo;

import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.dynamiclog.core.adapter.DefaultLoggingSystemAdapterRegistry;
import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.event.DefaultLogEventBus;
import com.dynamiclog.core.manager.DynamicLogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 {@link ApolloLogLevelListener} 已真正接通 Apollo 配置变更到主线
 * （此前该监听器为未接线的死代码）。
 * <p>
 * 通过构造真实的 {@link ConfigChangeEvent} 触发 {@code onChange}，
 * 并用真实 {@link DynamicLogManager} + 内存假适配器断言最终落到适配器上的级别。
 * 该链路比 mock 更贴近真实行为，且规避 mockito-inline 在 JDK 21 上 retransform 具体类的限制。
 */
class ApolloLogLevelListenerTest {

    private static final String NAMESPACE = "application";

    private InMemoryAdapter adapter;
    private ApolloLogLevelListener listener;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryAdapter();
        DefaultLoggingSystemAdapterRegistry registry = new DefaultLoggingSystemAdapterRegistry();
        registry.register(adapter);
        DynamicLogManager manager = new DynamicLogManager(registry, new DefaultLogEventBus());
        listener = new ApolloLogLevelListener(manager);
    }

    private ConfigChangeEvent event(Map<String, ConfigChange> changes) {
        return new ConfigChangeEvent(NAMESPACE, changes);
    }

    private ConfigChange change(String key, String oldValue, String newValue, PropertyChangeType type) {
        return new ConfigChange(NAMESPACE, key, oldValue, newValue, type);
    }

    @Test
    void appliesLoggingLevelChangeAndIgnoresNonLoggingKeys() {
        Map<String, ConfigChange> changes = new HashMap<>();
        changes.put("logging.level.com.foo",
                change("logging.level.com.foo", "INFO", "DEBUG", PropertyChangeType.MODIFIED));
        changes.put("server.port",
                change("server.port", "8080", "9090", PropertyChangeType.MODIFIED));

        listener.onChange(event(changes));

        assertEquals("DEBUG", adapter.getLogLevel("com.foo"));
        // 非 logging.level.* 的键应被忽略，不落到适配器
        assertFalse(adapter.appliedLevels.containsKey("server.port"));
        assertNull(adapter.getLogLevel("server.port"));
    }

    @Test
    void addedTypeIsAppliedAndValueUpperCased() {
        Map<String, ConfigChange> changes = new HashMap<>();
        // 小写新值验证监听器会规范化为大写
        changes.put("logging.level.com.baz",
                change("logging.level.com.baz", null, "warn", PropertyChangeType.ADDED));

        listener.onChange(event(changes));

        assertEquals("WARN", adapter.getLogLevel("com.baz"));
    }

    @Test
    void deletedOrNullValueIsIgnored() {
        Map<String, ConfigChange> changes = new HashMap<>();
        changes.put("logging.level.com.bar",
                change("logging.level.com.bar", "INFO", null, PropertyChangeType.DELETED));

        listener.onChange(event(changes));

        // 唯一变更是删除型（newValue 为 null），无有效项，不应改动适配器
        assertTrue(adapter.appliedLevels.isEmpty());
    }

    @Test
    void noLoggingKeysDoesNotTouchAdapter() {
        Map<String, ConfigChange> changes = new HashMap<>();
        changes.put("server.port",
                change("server.port", "8080", "9090", PropertyChangeType.MODIFIED));
        changes.put("spring.application.name",
                change("spring.application.name", "a", "b", PropertyChangeType.MODIFIED));

        listener.onChange(event(changes));

        assertTrue(adapter.appliedLevels.isEmpty());
    }

    @Test
    void emptyChangeSetDoesNotTouchAdapter() {
        listener.onChange(event(new HashMap<>()));

        assertTrue(adapter.appliedLevels.isEmpty());
    }

    /**
     * 内存假适配器：以 Map 记录 logger→level，供断言观察监听器最终应用的级别。
     */
    private static final class InMemoryAdapter implements LoggingSystemAdapter {

        final Map<String, String> appliedLevels = new ConcurrentHashMap<>();

        @Override
        public String getName() {
            return "in-memory";
        }

        @Override
        public void setLogLevel(String loggerName, String level) {
            if (level == null) {
                appliedLevels.remove(loggerName);
            } else {
                appliedLevels.put(loggerName, level);
            }
        }

        @Override
        public String getLogLevel(String loggerName) {
            return appliedLevels.get(loggerName);
        }

        @Override
        public Collection<String> getLoggerNames() {
            return new ArrayList<>(appliedLevels.keySet());
        }

        @Override
        public void resetLogLevel(String loggerName) {
            appliedLevels.remove(loggerName);
        }
    }
}
