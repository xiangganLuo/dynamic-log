package com.dynamiclog.endpoint;

import com.dynamiclog.core.manager.DynamicLogManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 动态日志 REST 端点自动配置。
 * <p>
 * 装配 {@link DynamicLogController}，通过 HTTP 在运行时查询/设置日志级别（含临时级别）。
 * <p>
 * 仅当 classpath 存在 Spring Web（{@code RestController}）、容器中存在
 * {@link DynamicLogManager}，且 {@code dynamic-log.endpoint.enabled} 未显式关闭时生效。
 * <p>
 * Controller 内部持有的回滚调度器实现了 {@code DisposableBean}，容器关闭时自动释放。
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
@ConditionalOnBean(DynamicLogManager.class)
@ConditionalOnProperty(prefix = "dynamic-log.endpoint", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicLogEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DynamicLogController dynamicLogController(DynamicLogManager manager) {
        return new DynamicLogController(manager);
    }
}
