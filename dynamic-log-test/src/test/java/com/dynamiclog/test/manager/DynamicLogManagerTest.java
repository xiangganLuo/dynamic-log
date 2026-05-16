package com.dynamiclog.test.manager;

import com.dynamiclog.core.adapter.DefaultLoggingSystemAdapterRegistry;
import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.event.DefaultLogEventBus;
import com.dynamiclog.core.event.EventType;
import com.dynamiclog.core.event.LogEvent;
import com.dynamiclog.core.event.LogEventListener;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.core.model.LogLevelChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DynamicLogManagerTest {

    private DefaultLoggingSystemAdapterRegistry registry;
    private DefaultLogEventBus eventBus;
    private DynamicLogManager manager;
    private MockAdapter adapter;

    @BeforeEach
    void setUp() {
        registry = new DefaultLoggingSystemAdapterRegistry();
        eventBus = new DefaultLogEventBus();
        adapter = new MockAdapter("logback");
        registry.register(adapter);
        manager = new DynamicLogManager(registry, eventBus);
    }

    @Test
    void applyLogLevelChange() {
        LogLevelChange change = LogLevelChange.builder()
                .putLevel("com.example", "DEBUG")
                .putLevel("com.example.service", "INFO")
                .build();

        manager.applyLogLevelChange(change);

        assertEquals("DEBUG", adapter.getLogLevel("com.example"));
        assertEquals("INFO", adapter.getLogLevel("com.example.service"));
    }

    @Test
    void emptyChangeSkips() {
        LogLevelChange change = LogLevelChange.builder().build();
        manager.applyLogLevelChange(change);
        assertTrue(adapter.appliedLevels.isEmpty());
    }

    @Test
    void nullChangeSkips() {
        manager.applyLogLevelChange(null);
        assertTrue(adapter.appliedLevels.isEmpty());
    }

    @Test
    void eventsPublishedOnSuccess() {
        List<LogEvent> events = new ArrayList<>();
        eventBus.subscribe(events::add);

        LogLevelChange change = LogLevelChange.builder()
                .putLevel("com.example", "DEBUG")
                .build();

        manager.applyLogLevelChange(change);

        assertEquals(2, events.size());
        assertEquals(EventType.LOG_LEVEL_CHANGE, events.get(0).getType());
        assertEquals(EventType.LOG_LEVEL_CHANGED, events.get(1).getType());
    }

    @Test
    void resetAllLogLevels() {
        adapter.setLogLevel("com.example", "DEBUG");
        adapter.setLogLevel("com.example.service", "INFO");

        manager.resetAllLogLevels();

        assertNull(adapter.getLogLevel("com.example"));
        assertNull(adapter.getLogLevel("com.example.service"));
    }

    static class MockAdapter implements LoggingSystemAdapter {
        private final String name;
        final Map<String, String> appliedLevels = new HashMap<>();

        MockAdapter(String name) { this.name = name; }
        @Override public String getName() { return name; }
        @Override public void setLogLevel(String loggerName, String level) { appliedLevels.put(loggerName, level); }
        @Override public String getLogLevel(String loggerName) { return appliedLevels.get(loggerName); }
        @Override public Collection<String> getLoggerNames() { return appliedLevels.keySet(); }
        @Override public void resetLogLevel(String loggerName) { appliedLevels.put(loggerName, null); }
    }
}
