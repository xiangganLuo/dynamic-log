package com.dynamiclog.ttl;

import com.dynamiclog.core.manager.DynamicLogManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TTL 临时日志级别自动配置。
 * <p>
 * 当容器中存在 {@link DynamicLogManager}，且 {@code dynamic-log.ttl.enabled} 未显式关闭时，
 * 暴露一个 {@link TemporaryLogLevelManager} Bean。{@code destroyMethod="shutdown"} 保证容器
 * 关闭时释放内部调度线程。
 */
@Configuration
@ConditionalOnBean(DynamicLogManager.class)
@ConditionalOnProperty(prefix = "dynamic-log.ttl", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicLogTtlAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public TemporaryLogLevelManager temporaryLogLevelManager(DynamicLogManager manager) {
        return new TemporaryLogLevelManager(manager);
    }
}
