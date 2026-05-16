package com.dynamiclog.spring.config;

import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.spring.refresher.SpringEnvironmentRefresher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置日志刷新策略。
 * <p>
 * 根据可用的依赖条件创建刷新器：
 * <ul>
 *   <li>Spring Environment 刷新器（Spring 环境下始终可用）</li>
 * </ul>
 * <p>
 * Spring Cloud 集成（EnvironmentChangeEvent 监听）需要用户自行添加
 * spring-cloud-context 依赖并实现对应的事件监听器。
 */
@Configuration
@ConditionalOnBean(DynamicLogManager.class)
public class DynamicLogRefresherAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "springEnvironmentRefresher")
    @ConditionalOnClass(LoggingSystem.class)
    public SpringEnvironmentRefresher springEnvironmentRefresher(
            DynamicLogManager logManager,
            ConfigurableApplicationContext applicationContext) {
        LoggingSystem loggingSystem = LoggingSystem.get(applicationContext.getClassLoader());
        SpringEnvironmentRefresher refresher = new SpringEnvironmentRefresher(logManager, applicationContext, loggingSystem);
        refresher.start();
        return refresher;
    }
}
