package com.agora.notification.exceptions;

/**
 * Exception thrown when validation of notification data fails.
 * This is distinct from provider errors - it indicates invalid input data.
 */
public class ValidationException extends NotificationException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
