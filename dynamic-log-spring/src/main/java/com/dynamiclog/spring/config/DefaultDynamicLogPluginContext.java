package com.dynamiclog.spring.config;

import com.dynamiclog.core.adapter.LoggingSystemAdapterRegistry;
import com.dynamiclog.core.event.LogEventBus;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.core.plugin.spi.PluginContext;
import com.dynamiclog.spring.properties.DynamicLogProperties;

import java.util.Map;

/**
 * Default implementation of {@link PluginContext} for Spring Boot integration.
 */
class DefaultDynamicLogPluginContext implements PluginContext {

    private final DynamicLogManager logManager;
    private final LoggingSystemAdapterRegistry adapterRegistry;
    private final LogEventBus eventBus;
    private final DynamicLogProperties properties;

    DefaultDynamicLogPluginContext(DynamicLogManager logManager,
                                    LoggingSystemAdapterRegistry adapterRegistry,
                                    LogEventBus eventBus,
                                    DynamicLogProperties properties) {
        this.logManager = logManager;
        this.adapterRegistry = adapterRegistry;
        this.eventBus = eventBus;
        this.properties = properties;
    }

    @Override
    public LoggingSystemAdapterRegistry getAdapterRegistry() {
        return adapterRegistry;
    }

    @Override
    public LogEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public DynamicLogManager getLogManager() {
        return logManager;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Map<String, String> levels = properties.getLevels();
        String value = levels.get(key);
        return value != null ? value : defaultValue;
    }
}
