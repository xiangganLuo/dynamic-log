package com.dynamiclog.core;

import com.dynamiclog.core.adapter.DefaultLoggingSystemAdapterRegistry;
import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.adapter.LoggingSystemAdapterRegistry;
import com.dynamiclog.core.event.DefaultLogEventBus;
import com.dynamiclog.core.event.LogEventBus;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.core.refresher.LogRefresher;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态日志框架的主要入口点和构建器。
 * <p>
 * 提供流畅的API来构建和配置DynamicLogManager，
 * 包括所需的适配器、刷新器和事件处理。
 *
 * <pre>{@code
 * DynamicLogManager manager = DynamicLog.builder()
 *     .adapter(new LogbackAdapter())
 *     .refresher(myRefresher)
 *     .build();
 * }</pre>
 */
public final class DynamicLog {

    private final List<LoggingSystemAdapter> adapters = new ArrayList<>();
    private final List<LogRefresher> refreshers = new ArrayList<>();
    private LoggingSystemAdapterRegistry adapterRegistry;
    private LogEventBus eventBus;
    private String defaultAdapterName;

    private DynamicLog() {
    }

    public static DynamicLog builder() {
        return new DynamicLog();
    }

    public DynamicLog adapter(LoggingSystemAdapter adapter) {
        adapters.add(adapter);
        return this;
    }

    public DynamicLog adapters(List<LoggingSystemAdapter> adapters) {
        this.adapters.addAll(adapters);
        return this;
    }

    public DynamicLog adapterRegistry(LoggingSystemAdapterRegistry registry) {
        this.adapterRegistry = registry;
        return this;
    }

    public DynamicLog eventBus(LogEventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    public DynamicLog defaultAdapter(String name) {
        this.defaultAdapterName = name;
        return this;
    }

    public DynamicLog refresher(LogRefresher refresher) {
        refreshers.add(refresher);
        return this;
    }

    /**
     * 构建并返回DynamicLogManager实例。
     * 注册所有适配器，启动所有刷新器。
     */
    public DynamicLogManager build() {
        if (adapterRegistry == null) {
            adapterRegistry = new DefaultLoggingSystemAdapterRegistry();
        }
        if (eventBus == null) {
            eventBus = new DefaultLogEventBus();
        }

        for (LoggingSystemAdapter adapter : adapters) {
            adapterRegistry.register(adapter);
        }

        if (defaultAdapterName != null) {
            adapterRegistry.setDefaultAdapter(defaultAdapterName);
        }

        DynamicLogManager manager = new DynamicLogManager(adapterRegistry, eventBus);

        for (LogRefresher refresher : refreshers) {
            refresher.start();
        }

        return manager;
    }
}
