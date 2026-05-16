package com.dynamiclog.core.plugin.manage;

import com.dynamiclog.core.plugin.spi.DynamicLogPlugin;

import java.util.Collection;

/**
 * 动态日志插件管理器。
 * <p>
 * 处理插件生命周期（初始化、启动、停止、销毁）和依赖排序。
 */
public interface PluginManager {

    /**
     * 注册并初始化插件。
     */
    void register(DynamicLogPlugin plugin);

    /**
     * 注销并销毁插件。
     */
    void unregister(String pluginId);

    /**
     * 按顺序启动所有已注册的插件。
     */
    void startAll();

    /**
     * 按相反顺序停止所有已注册的插件。
     */
    void stopAll();

    /**
     * 按ID返回插件。
     */
    DynamicLogPlugin getPlugin(String pluginId);

    /**
     * 返回所有已注册的插件。
     */
    Collection<DynamicLogPlugin> getPlugins();

    /**
     * 检查插件是否已注册。
     */
    boolean contains(String pluginId);
}
