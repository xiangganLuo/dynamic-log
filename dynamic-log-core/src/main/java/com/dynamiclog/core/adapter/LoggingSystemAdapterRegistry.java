package com.dynamiclog.core.adapter;

import java.util.Collection;

/**
 * {@link LoggingSystemAdapter} 实现的注册表。
 * <p>
 * 支持按名称注册多个适配器，允许运行时选择适当的日志系统适配器。
 */
public interface LoggingSystemAdapterRegistry {

    /**
     * 注册适配器。
     *
     * @param adapter 要注册的适配器
     * @throws IllegalArgumentException 如果已存在同名的适配器
     */
    void register(LoggingSystemAdapter adapter);

    /**
     * 按名称注销适配器。
     *
     * @param name 适配器名称
     * @return 被注销的适配器，如果未找到则返回null
     */
    LoggingSystemAdapter unregister(String name);

    /**
     * 按名称获取适配器。
     *
     * @param name 适配器名称
     * @return 适配器，如果未找到则返回null
     */
    LoggingSystemAdapter getAdapter(String name);

    /**
     * 返回默认适配器（第一个注册的或显式设置的）。
     */
    LoggingSystemAdapter getDefaultAdapter();

    /**
     * 按名称设置默认适配器。
     *
     * @param name 适配器名称
     */
    void setDefaultAdapter(String name);

    /**
     * 返回所有已注册的适配器名称。
     */
    Collection<String> getRegisteredNames();

    /**
     * 检查是否已注册给定名称的适配器。
     */
    boolean contains(String name);
}
