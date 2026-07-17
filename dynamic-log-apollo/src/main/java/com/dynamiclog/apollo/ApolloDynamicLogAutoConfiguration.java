package com.dynamiclog.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.dynamiclog.core.manager.DynamicLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Apollo 动态日志自动配置。
 * <p>
 * 面向 Apollo 原生接入：将 {@link ApolloLogLevelListener} 注册到 Apollo 的
 * 默认应用配置以及用户配置的命名空间。配置中心的日志级别变更会实时回调监听器，
 * 从而在运行时刷新日志级别。
 * <p>
 * 仅当 classpath 存在 Apollo（{@code ConfigService}）、容器中存在
 * {@link DynamicLogManager}，且 {@code dynamic-log.apollo.enabled} 未显式关闭时生效。
 */
@Configuration
@ConditionalOnClass(name = "com.ctrip.framework.apollo.ConfigService")
@ConditionalOnBean(DynamicLogManager.class)
@ConditionalOnProperty(prefix = "dynamic-log.apollo", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ApolloDynamicLogProperties.class)
public class ApolloDynamicLogAutoConfiguration implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(ApolloDynamicLogAutoConfiguration.class);

    /**
     * 默认应用命名空间名称，由 {@code ConfigService.getAppConfig()} 覆盖。
     */
    private static final String NAMESPACE_APPLICATION = "application";

    private final ApolloLogLevelListener listener;
    private final ApolloDynamicLogProperties properties;

    public ApolloDynamicLogAutoConfiguration(ApolloLogLevelListener listener,
                                             ApolloDynamicLogProperties properties) {
        this.listener = listener;
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ApolloLogLevelListener apolloLogLevelListener(DynamicLogManager manager) {
        return new ApolloLogLevelListener(manager);
    }

    /**
     * 在所有单例初始化完成后，向 Apollo 注册监听器。
     */
    @Override
    public void afterSingletonsInstantiated() {
        log.debug("开始向 Apollo 注册日志级别监听器, 配置命名空间列表={}", properties.getNamespaces());
        // 默认应用配置（namespace = application）始终注册
        log.debug("Apollo addChangeListener -> namespace=[{}] (getAppConfig)", NAMESPACE_APPLICATION);
        ConfigService.getAppConfig().addChangeListener(listener);
        log.info("已向 Apollo 默认命名空间 [{}] 注册日志级别监听器", NAMESPACE_APPLICATION);

        List<String> namespaces = properties.getNamespaces();
        if (namespaces != null) {
            for (String ns : namespaces) {
                if (ns == null || ns.trim().isEmpty() || NAMESPACE_APPLICATION.equals(ns.trim())) {
                    // application 已由 getAppConfig() 覆盖，跳过以避免重复注册
                    log.debug("Apollo 跳过命名空间 [{}]（空或与默认 application 重复）", ns);
                    continue;
                }
                log.debug("Apollo addChangeListener -> namespace=[{}] (getConfig)", ns.trim());
                Config config = ConfigService.getConfig(ns.trim());
                config.addChangeListener(listener);
                log.info("已向 Apollo 命名空间 [{}] 注册日志级别监听器", ns.trim());
            }
        }
    }
}
