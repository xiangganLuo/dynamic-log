package com.dynamiclog.test.adapter;

import com.dynamiclog.core.adapter.DefaultLoggingSystemAdapterRegistry;
import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.common.exception.AdapterNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DefaultLoggingSystemAdapterRegistryTest {

    private DefaultLoggingSystemAdapterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultLoggingSystemAdapterRegistry();
    }

    @Test
    void registerAndGetAdapter() {
        LoggingSystemAdapter adapter = createMockAdapter("logback");
        registry.register(adapter);

        assertEquals(adapter, registry.getAdapter("logback"));
        assertTrue(registry.contains("logback"));
    }

    @Test
    void firstRegisteredBecomesDefault() {
        LoggingSystemAdapter logback = createMockAdapter("logback");
        LoggingSystemAdapter log4j2 = createMockAdapter("log4j2");

        registry.register(logback);
        registry.register(log4j2);

        assertEquals(logback, registry.getDefaultAdapter());
    }

    @Test
    void setDefaultAdapter() {
        LoggingSystemAdapter logback = createMockAdapter("logback");
        LoggingSystemAdapter log4j2 = createMockAdapter("log4j2");

        registry.register(logback);
        registry.register(log4j2);
        registry.setDefaultAdapter("log4j2");

        assertEquals(log4j2, registry.getDefaultAdapter());
    }

    @Test
    void unregisterAdapter() {
        LoggingSystemAdapter adapter = createMockAdapter("logback");
        registry.register(adapter);

        LoggingSystemAdapter removed = registry.unregister("logback");
        assertEquals(adapter, removed);
        assertFalse(registry.contains("logback"));
    }

    @Test
    void getAdapterNotFoundThrows() {
        assertThrows(AdapterNotFoundException.class, () -> registry.getAdapter("nonexistent"));
    }

    @Test
    void duplicateRegistrationThrows() {
        registry.register(createMockAdapter("logback"));
        assertThrows(IllegalArgumentException.class, () -> registry.register(createMockAdapter("logback")));
    }

    @Test
    void getRegisteredNames() {
        registry.register(createMockAdapter("logback"));
        registry.register(createMockAdapter("log4j2"));

        Collection<String> names = registry.getRegisteredNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("logback"));
        assertTrue(names.contains("log4j2"));
    }

    private LoggingSystemAdapter createMockAdapter(String name) {
        return new LoggingSystemAdapter() {
            @Override public String getName() { return name; }
            @Override public void setLogLevel(String loggerName, String level) {}
            @Override public String getLogLevel(String loggerName) { return null; }
            @Override public Collection<String> getLoggerNames() { return Collections.emptyList(); }
            @Override public void resetLogLevel(String loggerName) {}
        };
    }
}
