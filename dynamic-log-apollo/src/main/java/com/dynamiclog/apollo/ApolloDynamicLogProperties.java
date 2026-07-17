package com.dynamiclog.apollo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Apollo 集成配置属性。
 *
 * <pre>
 * dynamic-log:
 *   apollo:
 *     enabled: true
 *     namespaces:
 *       - application
 *       - some-other-namespace
 * </pre>
 */
@ConfigurationProperties(prefix = "dynamic-log.apollo")
public class ApolloDynamicLogProperties {

    /**
     * 是否启用 Apollo 集成。
     */
    private boolean enabled = true;

    /**
     * 需要额外监听的命名空间列表。
     * <p>
     * 默认包含 {@code application}。默认命名空间（{@code application}）始终通过
     * {@code ConfigService.getAppConfig()} 注册，因此列表中的 {@code application}
     * 会被去重，不会重复注册。
     */
    private List<String> namespaces = new ArrayList<>(Collections.singletonList("application"));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }
}
