package com.dynamiclog.core.event;

/**
 * 动态日志框架中的事件类型。
 */
public enum EventType {

    LOG_LEVEL_CHANGE,
    LOG_LEVEL_CHANGED,
    REFRESHER_STARTED,
    REFRESHER_STOPPED,
    ADAPTER_REGISTERED,
    ADAPTER_UNREGISTERED,
    ERROR
}
