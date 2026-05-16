package com.dynamiclog.spring.config;

import com.dynamiclog.spring.properties.DynamicLogProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Dynamic Log 主自动配置入口。
 * <p>
 * 导入子配置实现细粒度的条件控制：
 * <ul>
 *   <li>{@link DynamicLogCoreAutoConfiguration} - 核心管理器和适配器注册表</li>
 *   <li>{@link DynamicLogRefresherAutoConfiguration} - 刷新策略</li>
 *   <li>{@link DynamicLogBannerAutoConfiguration} - 启动横幅</li>
 *   <li>{@link DynamicLogListenerAutoConfiguration} - Apollo/Nacos 监听器</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(DynamicLogProperties.class)
@Import({
        DynamicLogCoreAutoConfiguration.class,
        DynamicLogRefresherAutoConfiguration.class,
        DynamicLogBannerAutoConfiguration.class,
        DynamicLogListenerAutoConfiguration.class
})
public class DynamicLogAutoConfiguration {
}
