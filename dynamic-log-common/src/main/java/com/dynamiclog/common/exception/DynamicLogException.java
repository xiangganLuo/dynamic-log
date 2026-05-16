package com.dynamiclog.common.exception;

/**
 * Base exception for Dynamic Log framework.
 */
public class DynamicLogException extends RuntimeException {

    public DynamicLogException(String message) {
        super(message);
    }

    public DynamicLogException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DynamicLogException forAdapter(String loggingSystem, String reason) {
        return new DynamicLogException("Adapter error [" + loggingSystem + "]: " + reason);
    }

    public static DynamicLogException forConfig(String message) {
        return new DynamicLogException("Configuration error: " + message);
    }
}
