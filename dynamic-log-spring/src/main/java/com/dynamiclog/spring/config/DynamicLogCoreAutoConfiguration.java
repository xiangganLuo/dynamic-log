package com.dynamiclog.spring.config;

import com.dynamiclog.core.adapter.DefaultLoggingSystemAdapterRegistry;
import com.dynamiclog.core.adapter.LoggingSystemAdapter;
import com.dynamiclog.core.adapter.LoggingSystemAdapterRegistry;
import com.dynamiclog.core.event.DefaultLogEventBus;
import com.dynamiclog.core.event.LogEventBus;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.core.plugin.manage.DefaultPluginManager;
import com.dynamiclog.core.plugin.manage.PluginManager;
import com.dynamiclog.core.plugin.spi.DynamicLogPlugin;
import com.dynamiclog.core.plugin.spi.PluginContext;
import com.dynamiclog.spring.adapter.LogbackSpringAdapter;
import com.dynamiclog.spring.lifecycle.DynamicLogPluginLifecycle;
import com.dynamiclog.spring.properties.DynamicLogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static final Logger log = LoggerFactory.getLogger(DynamicLogCoreAutoConfiguration.class);

    /**
     * 基于 Spring Boot {@link LoggingSystem} 的 Logback 适配器。
     * <p>
     * 这是 Spring 环境下默认注册的适配器，名称为 "logback"，
     * 使框架在 Spring Boot 中开箱即用。
     */
    @Bean
    @ConditionalOnMissingBean(LoggingSystemAdapter.class)
    public LogbackSpringAdapter logbackSpringAdapter(ConfigurableApplicationContext ctx) {
        return new LogbackSpringAdapter(LoggingSystem.get(ctx.getClassLoader()));
    }

    /**
     * 适配器注册表。
     * <p>
     * 收集容器中所有 {@link LoggingSystemAdapter} 类型的 Bean 并注册，
     * 默认适配器为第一个注册的适配器（可由 {@code dynamic-log.default-adapter} 覆盖）。
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingSystemAdapterRegistry loggingSystemAdapterRegistry(ObjectProvider<LoggingSystemAdapter> adapters) {
        DefaultLoggingSystemAdapterRegistry registry = new DefaultLoggingSystemAdapterRegistry();
        adapters.orderedStream().forEach(adapter -> {
            if (log.isDebugEnabled()) {
                log.debug("收集并注册日志适配器: name={}, class={}", adapter.getName(), adapter.getClass().getName());
            }
            registry.register(adapter);
        });
        if (log.isDebugEnabled()) {
            log.debug("适配器注册完成: 已注册={}, 默认适配器={}",
                    registry.getRegisteredNames(), registry.getRegisteredNames().isEmpty() ? "无" : registry.getDefaultAdapter().getName());
        }
        return registry;
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
        List<DynamicLogPlugin> pluginList = new ArrayList<>(plugins.orderedStream().collect(Collectors.toList()));
        return new DynamicLogPluginLifecycle(pluginManager, pluginList);
    }

    /**
     * 启动时应用 {@code dynamic-log.levels} 配置的日志级别。
     * <p>
     * 采用 additive 方式（{@link LoggingSystemAdapter#applyLevels}）逐个设置，
     * 不走 {@code applyLogLevelChange}，以免其 {@code resetAbsentLoggers}
     * 把 Spring Boot 已配好的 {@code logging.level.*} 冲掉。
     */
    @Bean
    public ApplicationRunner dynamicLogLevelsInitializer(DynamicLogManager manager, DynamicLogProperties properties) {
        return args -> {
            Map<String, String> levels = properties.getLevels();
            if (levels != null && !levels.isEmpty()) {
                LoggingSystemAdapter target = manager.getAdapterRegistry().getDefaultAdapter();
                if (log.isDebugEnabled()) {
                    log.debug("启动应用 dynamic-log.levels: 共 {} 项 {} -> 适配器 [{}]",
                            levels.size(), levels, target.getName());
                }
                target.applyLevels(levels);
            } else {
                log.debug("dynamic-log.levels 为空，跳过启动级别应用");
            }
        };
    }
}
