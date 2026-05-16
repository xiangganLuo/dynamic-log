package com.dynamiclog.spring.listener;

import com.dynamiclog.common.constants.DynamicLogConstants;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.core.model.LogLevelChange;
import com.dynamiclog.core.refresher.LogRefresher;
import com.dynamiclog.spring.refresher.SpringEnvironmentRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Apollo 配置变更监听器。
 * <p>
 * 自动监听 Apollo 配置中心的日志级别变更，
 * 无需用户手动实现监听逻辑。
 * <p>
 * 使用方式：添加 Apollo 依赖即可自动生效
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.ctrip.framework.apollo&lt;/groupId&gt;
 *     &lt;artifactId&gt;apollo-client&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
public class ApolloConfigChangeListener {

    private static final Logger log = LoggerFactory.getLogger(ApolloConfigChangeListener.class);

    @Autowired
    private SpringEnvironmentRefresher refresher;

    /**
     * 处理 Apollo 配置变更事件。
     * 通过反射调用，避免直接依赖 Apollo API。
     */
    public void onChange(Set<String> changedKeys) {
        boolean loggingChanged = changedKeys.stream()
                .anyMatch(key -> key.startsWith(DynamicLogConstants.LOGGING_PROPERTY_PREFIX));

        if (loggingChanged) {
            log.info("Apollo 检测到日志配置变更: {}", changedKeys);
            refresher.refresh();
        }
    }
}
