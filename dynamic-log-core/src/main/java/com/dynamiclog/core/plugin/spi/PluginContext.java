package com.dynamiclog.core.plugin.spi;

import com.dynamiclog.core.adapter.LoggingSystemAdapterRegistry;
import com.dynamiclog.core.event.LogEventBus;
import com.dynamiclog.core.manager.DynamicLogManager;

/**
 * 在初始化期间提供给插件的上下文。
 * <p>
 * 在不暴露完整管理器API的情况下，提供对框架内部的可控访问。
 */
public interface PluginContext {

    /**
     * 返回适配器注册表。
     */
    LoggingSystemAdapterRegistry getAdapterRegistry();

    /**
     * 返回事件总线。
     */
    LogEventBus getEventBus();

    /**
     * 返回日志管理器。
     */
    DynamicLogManager getLogManager();

    /**
     * 返回插件特定的属性。
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    String getProperty(String key, String defaultValue);
}
