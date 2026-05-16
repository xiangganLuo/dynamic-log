package com.dynamiclog.spring.banner;

import com.dynamiclog.common.constants.DynamicLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Prints the Dynamic Log banner on application startup.
 */
public class DynamicLogBanner {

    private static final Logger log = LoggerFactory.getLogger(DynamicLogBanner.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("{}", DynamicLogConstants.BANNER_TEXT);
        log.info("Dynamic Log framework initialized successfully");
    }
}
