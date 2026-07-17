package com.dynamiclog.core.manager;

import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.adapter.LoggingSystemAdapterRegistry;
import com.dynamiclog.core.event.EventType;
import com.dynamiclog.core.event.LogEvent;
import com.dynamiclog.core.event.LogEventBus;
import com.dynamiclog.core.model.LogLevelChange;
import com.dynamiclog.core.plugin.manage.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 动态日志框架的主外观类。
 * <p>
 * 协调适配器注册表、事件总线和刷新策略，
 * 提供统一的 API 来管理动态日志级别。
 * <p>
 * 实现外观模式，简化与子系统的交互。
 */
public class DynamicLogManager {

    private static final Logger log = LoggerFactory.getLogger(DynamicLogManager.class);

    private final LoggingSystemAdapterRegistry adapterRegistry;
    private final LogEventBus eventBus;
    private volatile PluginManager pluginManager;

    public DynamicLogManager(LoggingSystemAdapterRegistry adapterRegistry, LogEventBus eventBus) {
        this.adapterRegistry = adapterRegistry;
        this.eventBus = eventBus;
    }

    /**
     * 设置插件管理器，建立与插件系统的连接。
     */
    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * 获取插件管理器。
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * 使用默认适配器应用日志级别变更。
     */
    public void applyLogLevelChange(LogLevelChange change) {
        applyLogLevelChange(change, null);
    }

    /**
     * 使用指定适配器应用日志级别变更。
     *
     * @param change      日志级别变更
     * @param adapterName 适配器名称，null 表示使用默认
     */
    public void applyLogLevelChange(LogLevelChange change, String adapterName) {
        if (change == null || change.isEmpty()) {
            log.debug("空的日志级别变更，跳过");
            return;
        }

        LoggingSystemAdapter adapter = resolveAdapter(adapterName);
        String usedAdapter = adapterName != null ? adapterName : adapter.getName();

        log.info("通过适配器 [{}] 应用 {} 个日志级别变更", usedAdapter, change.getLevelMap().size());

        eventBus.publish(LogEvent.builder()
                .type(EventType.LOG_LEVEL_CHANGE)
                .attribute("change", change)
                .attribute("adapter", usedAdapter)
                .build());

        try {
            adapter.applyLevels(change.getLevelMap());
            adapter.resetAbsentLoggers(change.getLevelMap().keySet());

            eventBus.publish(LogEvent.builder()
                    .type(EventType.LOG_LEVEL_CHANGED)
                    .attribute("change", change)
                    .attribute("adapter", usedAdapter)
                    .build());

            log.info("日志级别变更应用成功");
        } catch (Exception e) {
            log.error("通过适配器 [{}] 应用日志级别变更失败", usedAdapter, e);
            eventBus.publish(LogEvent.builder()
                    .type(EventType.ERROR)
                    .attribute("change", change)
                    .attribute("adapter", usedAdapter)
                    .attribute("error", e)
                    .build());
            throw e;
        }
    }

    /**
     * 重置所有日志级别为默认值。
     */
    public void resetAllLogLevels() {
        resetAllLogLevels(null);
    }

    /**
     * 使用指定适配器重置所有日志级别。
     */
    public void resetAllLogLevels(String adapterName) {
        LoggingSystemAdapter adapter = resolveAdapter(adapterName);
        adapter.getLoggerNames().stream()
                .filter(name -> !"ROOT".equalsIgnoreCase(name))
                .forEach(adapter::resetLogLevel);
        log.info("通过适配器 [{}] 重置所有日志级别", adapter.getName());
    }

    private LoggingSystemAdapter resolveAdapter(String adapterName) {
        if (adapterName != null) {
            log.debug("解析指定适配器: {}", adapterName);
            return adapterRegistry.getAdapter(adapterName);
        }
        LoggingSystemAdapter adapter = adapterRegistry.getDefaultAdapter();
        log.debug("解析默认适配器: {}", adapter.getName());
        return adapter;
    }

    public LoggingSystemAdapterRegistry getAdapterRegistry() {
        return adapterRegistry;
    }

    public LogEventBus getEventBus() {
        return eventBus;
    }
}
