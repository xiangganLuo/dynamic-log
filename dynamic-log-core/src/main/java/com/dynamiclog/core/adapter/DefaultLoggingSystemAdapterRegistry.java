package com.dynamiclog.core.adapter;

import com.dynamiclog.common.exception.AdapterNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link LoggingSystemAdapterRegistry} 的默认线程安全实现。
 * <p>
 * 使用ConcurrentHashMap进行并发访问。默认适配器是第一个注册的适配器，
 * 除非显式覆盖。
 */
public class DefaultLoggingSystemAdapterRegistry implements LoggingSystemAdapterRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultLoggingSystemAdapterRegistry.class);

    private final Map<String, LoggingSystemAdapter> adapters = new ConcurrentHashMap<>();
    private volatile String defaultAdapterName;

    @Override
    public void register(LoggingSystemAdapter adapter) {
        String name = adapter.getName();
        if (adapters.containsKey(name)) {
            throw new IllegalArgumentException("Adapter already registered: " + name);
        }
        adapters.put(name, adapter);
        if (defaultAdapterName == null) {
            defaultAdapterName = name;
        }
        log.info("Registered logging system adapter: {}", name);
    }

    @Override
    public LoggingSystemAdapter unregister(String name) {
        LoggingSystemAdapter removed = adapters.remove(name);
        if (removed != null && name.equals(defaultAdapterName)) {
            defaultAdapterName = adapters.isEmpty() ? null : adapters.keySet().iterator().next();
        }
        return removed;
    }

    @Override
    public LoggingSystemAdapter getAdapter(String name) {
        LoggingSystemAdapter adapter = adapters.get(name);
        if (adapter == null) {
            throw AdapterNotFoundException.forType(name);
        }
        return adapter;
    }

    @Override
    public LoggingSystemAdapter getDefaultAdapter() {
        if (defaultAdapterName == null) {
            throw AdapterNotFoundException.forType("default (no adapter registered)");
        }
        return getAdapter(defaultAdapterName);
    }

    @Override
    public void setDefaultAdapter(String name) {
        if (!adapters.containsKey(name)) {
            throw AdapterNotFoundException.forType(name);
        }
        this.defaultAdapterName = name;
    }

    @Override
    public Collection<String> getRegisteredNames() {
        return Collections.unmodifiableSet(adapters.keySet());
    }

    @Override
    public boolean contains(String name) {
        return adapters.containsKey(name);
    }
}
