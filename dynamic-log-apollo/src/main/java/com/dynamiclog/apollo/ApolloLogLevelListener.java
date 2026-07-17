package com.dynamiclog.apollo;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.dynamiclog.common.constants.DynamicLogConstants;
import com.dynamiclog.core.manager.DynamicLogManager;
import com.dynamiclog.core.model.LogLevelChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Apollo 配置变更监听器。
 * <p>
 * 监听 Apollo 命名空间中以 {@code logging.level.} 开头的键变更，
 * 将其转换为 {@link LogLevelChange} 并交由 {@link DynamicLogManager} 应用。
 * <p>
 * 删除型变更（新值为 {@code null}，即 DELETED 类型）暂时忽略——
 * 由于本监听器仅收到「变更」子集而非完整配置视图，无法安全地推断
 * 被删除键应回退到的级别，故留待后续版本按需支持。
 */
public class ApolloLogLevelListener implements ConfigChangeListener {

    private static final Logger log = LoggerFactory.getLogger(ApolloLogLevelListener.class);

    /**
     * 日志级别键前缀，例如 {@code logging.level.com.example}。
     */
    private static final String LEVEL_KEY_PREFIX = DynamicLogConstants.LOGGING_PROPERTY_PREFIX + ".";

    private final DynamicLogManager manager;

    public ApolloLogLevelListener(DynamicLogManager manager) {
        this.manager = manager;
    }

    @Override
    public void onChange(ConfigChangeEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Apollo onChange 收到变更, changedKeys={}", event.changedKeys());
        }
        Map<String, String> levels = new HashMap<>();
        for (String key : event.changedKeys()) {
            if (!key.startsWith(LEVEL_KEY_PREFIX)) {
                continue;
            }
            ConfigChange change = event.getChange(key);
            String newValue = change != null ? change.getNewValue() : null;
            if (log.isDebugEnabled()) {
                String oldValue = change != null ? change.getOldValue() : null;
                log.debug("Apollo 日志级别变更键: {} ({} -> {})", key, oldValue, newValue);
            }
            // 删除型变更（newValue == null）暂忽略
            if (newValue == null || newValue.trim().isEmpty()) {
                log.debug("Apollo 键 {} 新值为空（删除型变更），忽略", key);
                continue;
            }
            String loggerName = key.substring(LEVEL_KEY_PREFIX.length());
            levels.put(loggerName, newValue.trim().toUpperCase());
        }

        if (levels.isEmpty()) {
            log.debug("Apollo 本次变更无有效 logging.level.* 项，忽略");
            return;
        }

        log.info("Apollo 检测到日志级别变更: {}", levels);
        log.debug("Apollo 提交 applyLogLevelChange, 解析出 logger->level={}", levels);
        manager.applyLogLevelChange(LogLevelChange.builder().putAllLevels(levels).build());
    }
}
