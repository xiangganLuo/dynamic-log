package com.dynamiclog.nacos;

import com.dynamiclog.common.constants.DynamicLogConstants;
import com.dynamiclog.spring.refresher.SpringEnvironmentRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

import java.util.Set;

/**
 * Nacos 日志级别变更监听器。
 * <p>
 * 面向 {@code spring-cloud-alibaba-nacos-config} 的标准接入链路：
 * <pre>
 * Nacos 配置变更 → 更新 Spring Environment → 发布 {@link EnvironmentChangeEvent}
 *   → 本监听器检测到 logging.level.* 键变化 → {@link SpringEnvironmentRefresher#refresh()}
 *   → 从 Environment 重新读取并应用日志级别
 * </pre>
 * 因此无需直接依赖 Nacos SDK 的原生监听器，即可复用 Spring Cloud 的配置刷新机制。
 */
public class NacosLogLevelChangeListener implements ApplicationListener<EnvironmentChangeEvent> {

    private static final Logger log = LoggerFactory.getLogger(NacosLogLevelChangeListener.class);

    private final SpringEnvironmentRefresher refresher;

    public NacosLogLevelChangeListener(SpringEnvironmentRefresher refresher) {
        this.refresher = refresher;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        if (log.isDebugEnabled()) {
            log.debug("Nacos 收到 EnvironmentChangeEvent, keys={}", keys);
        }
        if (keys == null || keys.isEmpty()) {
            log.debug("Nacos EnvironmentChangeEvent 无变更键，跳过刷新");
            return;
        }
        boolean loggingChanged = keys.stream()
                .anyMatch(key -> key != null && key.startsWith(DynamicLogConstants.LOGGING_PROPERTY_PREFIX));
        log.debug("Nacos 变更键是否含 {}* : {}", DynamicLogConstants.LOGGING_PROPERTY_PREFIX, loggingChanged);
        if (loggingChanged) {
            log.info("Nacos 检测到日志级别配置变更: {}", keys);
            log.debug("Nacos 触发 SpringEnvironmentRefresher.refresh()");
            refresher.refresh();
        } else {
            log.debug("Nacos 本次变更不含 logging.level* 键，不触发刷新");
        }
    }
}
