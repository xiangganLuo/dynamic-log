package com.dynamiclog.test.spring;

import com.dynamiclog.audit.AuditLogPlugin;
import com.dynamiclog.audit.DynamicLogAuditAutoConfiguration;
import com.dynamiclog.core.adapter.LoggingSystemAdapterRegistry;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.spring.config.DynamicLogAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 Spring 集成主线在自动配置下真正接通：
 * <ul>
 *   <li>registry 中已注册 "logback" 适配器，且为默认适配器；</li>
 *   <li>{@code dynamic-log.levels} 会通过启动 Runner 应用到默认适配器。</li>
 * </ul>
 */
class DynamicLogSpringIntegrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DynamicLogAutoConfiguration.class));

    @Test
    void logbackAdapterIsRegisteredAsDefault() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(LoggingSystemAdapterRegistry.class);
            LoggingSystemAdapterRegistry registry = ctx.getBean(LoggingSystemAdapterRegistry.class);
            assertThat(registry.contains("logback")).isTrue();
            assertThat(registry.getDefaultAdapter().getName()).isEqualTo("logback");
        });
    }

    @Test
    void levelsAreAppliedOnStartup() {
        runner.withPropertyValues("dynamic-log.levels.com.dynamiclog.test.sample=DEBUG")
                .run(ctx -> {
                    // ApplicationContextRunner 不会自动执行 ApplicationRunner，手动触发以模拟启动
                    ApplicationRunner initializer = ctx.getBean("dynamicLogLevelsInitializer", ApplicationRunner.class);
                    initializer.run(new DefaultApplicationArguments());

                    DynamicLogManager manager = ctx.getBean(DynamicLogManager.class);
                    String level = manager.getAdapterRegistry()
                            .getDefaultAdapter()
                            .getLogLevel("com.dynamiclog.test.sample");
                    assertThat(level).isEqualTo("DEBUG");
                });
    }

    /**
     * 引入 audit 自动配置后，{@link AuditLogPlugin} Bean 应存在，
     * 并被插件生命周期（SmartLifecycle）在上下文刷新时自动注册并启动。
     */
    @Test
    void auditPluginIsRegisteredAndStartedViaLifecycle() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DynamicLogAutoConfiguration.class,
                        DynamicLogAuditAutoConfiguration.class))
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(AuditLogPlugin.class);

                    DynamicLogManager manager = ctx.getBean(DynamicLogManager.class);
                    assertThat(manager.getPluginManager()).isNotNull();
                    // 生命周期已 register + startAll，插件应在 PluginManager 中可见
                    assertThat(manager.getPluginManager().contains("audit-log-plugin")).isTrue();
                });
    }
}
