package com.dynamiclog.spring.listener;

import com.dynamiclog.common.constants.DynamicLogConstants;
import com.dynamiclog.spring.refresher.SpringEnvironmentRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Nacos 配置变更监听器。
 * <p>
 * 自动监听 Nacos 配置中心的日志级别变更，
 * 无需用户手动实现监听逻辑。
 * <p>
 * 使用方式：添加 Nacos 依赖即可自动生效
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.alibaba.cloud&lt;/groupId&gt;
 *     &lt;artifactId&gt;spring-cloud-starter-alibaba-nacos-config&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
public class NacosConfigChangeListener {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigChangeListener.class);

    @Autowired
    private SpringEnvironmentRefresher refresher;

    /**
     * 处理 Nacos 配置变更事件。
     * 通过反射调用，避免直接依赖 Nacos API。
     */
    public void onChange(String dataId, String group, String content) {
        log.info("Nacos 检测到配置变更 - dataId: {}, group: {}", dataId, group);

        // 检查是否是日志相关配置
        if (dataId != null && (dataId.contains("log") || dataId.contains("application"))) {
            log.info("检测到日志配置变更，触发刷新");
            refresher.refresh();
        }
    }
}
