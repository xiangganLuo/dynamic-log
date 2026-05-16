package com.dynamiclog.common.exception;

/**
 * Thrown when no suitable logging system adapter is found.
 */
public class AdapterNotFoundException extends DynamicLogException {

    private final String adapterType;

    private AdapterNotFoundException(String message, String adapterType) {
        super(message);
        this.adapterType = adapterType;
    }

    public static AdapterNotFoundException forType(String adapterType) {
        return new AdapterNotFoundException(
                "No LoggingSystemAdapter found for type: " + adapterType, adapterType);
    }

    public String getAdapterType() {
        return adapterType;
    }
}
