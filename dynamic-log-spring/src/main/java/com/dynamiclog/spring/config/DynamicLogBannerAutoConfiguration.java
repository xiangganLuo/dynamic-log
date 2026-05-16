package com.dynamiclog.spring.config;

import com.dynamiclog.spring.banner.DynamicLogBanner;
import com.dynamiclog.spring.properties.DynamicLogProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the Dynamic Log startup banner.
 */
@Configuration
@ConditionalOnProperty(prefix = "dynamic-log", name = "banner-enabled", havingValue = "true", matchIfMissing = true)
public class DynamicLogBannerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DynamicLogBanner dynamicLogBanner() {
        return new DynamicLogBanner();
    }
}
