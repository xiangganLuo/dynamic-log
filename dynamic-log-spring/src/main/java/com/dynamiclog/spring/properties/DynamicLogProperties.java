package com.dynamiclog.spring.properties;

import com.dynamiclog.common.constants.DynamicLogConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for the Dynamic Log framework.
 *
 * <pre>
 * dynamic-log:
 *   enabled: true
 *   banner-enabled: true
 *   default-adapter: logback
 *   levels:
 *     com.example: DEBUG
 *     com.example.service: INFO
 * </pre>
 */
@ConfigurationProperties(prefix = "dynamic-log")
public class DynamicLogProperties {

    /**
     * Whether the dynamic log framework is enabled.
     */
    private boolean enabled = true;

    /**
     * Whether to print the banner on startup.
     */
    private boolean bannerEnabled = true;

    /**
     * The default adapter name (e.g., "logback", "log4j2").
     */
    private String defaultAdapter = "logback";

    /**
     * Log level map: logger name -> level.
     */
    private Map<String, String> levels = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBannerEnabled() {
        return bannerEnabled;
    }

    public void setBannerEnabled(boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
    }

    public String getDefaultAdapter() {
        return defaultAdapter;
    }

    public void setDefaultAdapter(String defaultAdapter) {
        this.defaultAdapter = defaultAdapter;
    }

    public Map<String, String> getLevels() {
        return levels;
    }

    public void setLevels(Map<String, String> levels) {
        this.levels = levels;
    }
}
