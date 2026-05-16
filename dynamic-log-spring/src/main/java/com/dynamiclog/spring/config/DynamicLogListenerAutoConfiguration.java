package com.dynamiclog.spring.config;

import com.dynamiclog.spring.listener.ApolloConfigChangeListener;
import com.dynamiclog.spring.listener.NacosConfigChangeListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置变更监听器自动配置。
 * <p>
 * 根据 classpath 中的依赖自动注册对应的监听器：
 * <ul>
 *   <li>Apollo - 当检测到 apollo-client 时自动注册</li>
 *   <li>Nacos - 当检测到 nacos-client 时自动注册</li>
 * </ul>
 * <p>
 * 用户也可以自定义监听器覆盖默认实现。
 */
@Configuration
public class DynamicLogListenerAutoConfiguration {

    /**
     * Apollo 监听器自动配置。
     * 当 classpath 中存在 Apollo Client 时自动生效。
     */
    @Configuration
    @ConditionalOnClass(name = "com.ctrip.framework.apollo.ConfigChangeListener")
    static class ApolloListenerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ApolloConfigChangeListener apolloConfigChangeListener() {
            return new ApolloConfigChangeListener();
        }
    }

    /**
     * Nacos 监听器自动配置。
     * 当 classpath 中存在 Nacos Client 时自动生效。
     */
    @Configuration
    @ConditionalOnClass(name = "com.alibaba.nacos.api.config.listener.Listener")
    static class NacosListenerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public NacosConfigChangeListener nacosConfigChangeListener() {
            return new NacosConfigChangeListener();
        }
    }
}
