package com.dynamiclog.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 审计日志插件自动配置。
 * <p>
 * 暴露 {@link AuditLogPlugin}，它会被 dynamic-log-spring 的插件生命周期
 * （{@code DynamicLogPluginLifecycle}）自动注册并启动，无需使用方手动干预。
 * <p>
 * 仅当 {@code dynamic-log.audit.enabled} 未显式关闭（默认开启）时生效。
 */
@Configuration
@ConditionalOnProperty(prefix = "dynamic-log.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DynamicLogAuditProperties.class)
public class DynamicLogAuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditLogPlugin auditLogPlugin(DynamicLogAuditProperties properties) {
        return new AuditLogPlugin(properties);
    }
}
