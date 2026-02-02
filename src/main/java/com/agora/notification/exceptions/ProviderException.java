package com.agora.notification.exceptions;

/**
 * Exception thrown when a provider fails to send a notification.
 * This indicates an error from the external service, not invalid input.
 */
public class ProviderException extends NotificationException {

    private final String providerName;

    public ProviderException(String providerName, String message) {
        super(String.format("Provider [%s] error: %s", providerName, message));
        this.providerName = providerName;
    }

    public ProviderException(String providerName, String message, Throwable cause) {
        super(String.format("Provider [%s] error: %s", providerName, message), cause);
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }
}
