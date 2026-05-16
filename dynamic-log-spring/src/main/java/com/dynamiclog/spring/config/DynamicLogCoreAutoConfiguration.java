package com.dynamiclog.spring.config;

import com.dynamiclog.core.adapter.DefaultLoggingSystemAdapterRegistry;
import com.dynamiclog.core.adapter.LoggingSystemAdapterRegistry;
import com.dynamiclog.core.event.DefaultLogEventBus;
import com.dynamiclog.core.event.LogEventBus;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.core.plugin.manage.DefaultPluginManager;
import com.dynamiclog.core.plugin.manage.PluginManager;
import com.dynamiclog.core.plugin.spi.DynamicLogPlugin;
import com.dynamiclog.core.plugin.spi.PluginContext;
import com.dynamiclog.spring.lifecycle.DynamicLogPluginLifecycle;
import com.dynamiclog.spring.properties.DynamicLogProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dynamic Log 核心组件自动配置。
 * <p>
 * 创建适配器注册表、事件总线、管理器和插件管理器。
 * 所有 Bean 都支持用户自定义覆盖。
 * <p>
 * 自动收集用户定义的 {@link DynamicLogPlugin} 实现，
 * 并通过 {@link DynamicLogPluginLifecycle} 管理插件生命周期。
 */
@Configuration
@ConditionalOnProperty(prefix = "dynamic-log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicLogCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LoggingSystemAdapterRegistry loggingSystemAdapterRegistry() {
        return new DefaultLoggingSystemAdapterRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogEventBus logEventBus() {
        return new DefaultLogEventBus();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicLogManager dynamicLogManager(LoggingSystemAdapterRegistry adapterRegistry,
                                                LogEventBus eventBus,
                                                DynamicLogProperties properties) {
        if (properties.getDefaultAdapter() != null && adapterRegistry.contains(properties.getDefaultAdapter())) {
            adapterRegistry.setDefaultAdapter(properties.getDefaultAdapter());
        }
        return new DynamicLogManager(adapterRegistry, eventBus);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginManager dynamicLogPluginManager(DynamicLogManager logManager,
                                                  LoggingSystemAdapterRegistry adapterRegistry,
                                                  LogEventBus eventBus,
                                                  DynamicLogProperties properties) {
        PluginContext context = new DefaultDynamicLogPluginContext(logManager, adapterRegistry, eventBus, properties);
        DefaultPluginManager pluginManager = new DefaultPluginManager(context);
        // 建立 DynamicLogManager 与 PluginManager 的双向连接
        logManager.setPluginManager(pluginManager);
        return pluginManager;
    }

    /**
     * 插件生命周期管理器。
     * <p>
     * 自动收集所有 {@link DynamicLogPlugin} 类型的 Bean，
     * 在应用启动时注册并启动插件，在关闭时停止插件。
     */
    @Bean
    @ConditionalOnMissingBean
    public DynamicLogPluginLifecycle dynamicLogPluginLifecycle(
            PluginManager pluginManager,
            ObjectProvider<DynamicLogPlugin> plugins) {
        List<DynamicLogPlugin> pluginList = new ArrayList<>(plugins.orderedStream().toList());
        return new DynamicLogPluginLifecycle(pluginManager, pluginList);
    }
}
