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
 * </ul>
 * <p>
 * 配置中心接入（Apollo / Nacos）已拆分为独立模块
 * {@code dynamic-log-apollo} / {@code dynamic-log-nacos}，按需引入。
 */
@Configuration
@EnableConfigurationProperties(DynamicLogProperties.class)
@Import({
        DynamicLogCoreAutoConfiguration.class,
        DynamicLogRefresherAutoConfiguration.class,
        DynamicLogBannerAutoConfiguration.class
})
public class DynamicLogAutoConfiguration {
}
