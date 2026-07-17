package com.dynamiclog.nacos;

import com.dynamiclog.spring.refresher.SpringEnvironmentRefresher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Nacos 动态日志自动配置。
 * <p>
 * 面向 {@code spring-cloud-alibaba-nacos-config} 标准接入：注册
 * {@link NacosLogLevelChangeListener} 监听 Spring Cloud 的
 * {@code EnvironmentChangeEvent}，在 Nacos 配置变更导致 Environment 刷新时，
 * 触发 {@link SpringEnvironmentRefresher} 重新应用日志级别。
 * <p>
 * 仅当 classpath 同时存在 Nacos（{@code com.alibaba.nacos.api.config.listener.Listener}）
 * 与 Spring Cloud Context（{@code EnvironmentChangeEvent}）、容器中存在
 * {@link SpringEnvironmentRefresher}，且 {@code dynamic-log.nacos.enabled} 未显式关闭时生效。
 */
@Configuration
@ConditionalOnClass(name = {
        "com.alibaba.nacos.api.config.listener.Listener",
        "org.springframework.cloud.context.environment.EnvironmentChangeEvent"
})
@ConditionalOnBean(SpringEnvironmentRefresher.class)
@ConditionalOnProperty(prefix = "dynamic-log.nacos", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NacosDynamicLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NacosLogLevelChangeListener nacosLogLevelChangeListener(SpringEnvironmentRefresher refresher) {
        return new NacosLogLevelChangeListener(refresher);
    }
}
