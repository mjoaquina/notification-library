package com.agora.notification.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    private static final String TEST_MESSAGE = "test message";
    private static final String WRAPPER_MESSAGE = "wrapper";

    @Test
    void exceptionHierarchy_allCustomExceptionsExtendNotificationExceptionAndRuntimeException() {
        assertTrue(new NotificationException(TEST_MESSAGE) instanceof RuntimeException);
        assertTrue(new ProviderException("P", "m") instanceof NotificationException);
        assertTrue(new ProviderException("P", "m") instanceof RuntimeException);
        assertTrue(new ValidationException(TEST_MESSAGE) instanceof NotificationException);
        assertTrue(new ValidationException(TEST_MESSAGE) instanceof RuntimeException);
    }

    @Test
    void notificationException_withMessage() {
        NotificationException e = new NotificationException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void notificationException_withMessageAndCause() {
        Throwable cause = new RuntimeException("cause");
        NotificationException e = new NotificationException(WRAPPER_MESSAGE, cause);
        assertEquals(WRAPPER_MESSAGE, e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    void providerException_withMessage() {
        ProviderException e = new ProviderException("SendGrid", "API error");
        assertTrue(e.getMessage().contains("SendGrid"));
        assertTrue(e.getMessage().contains("API error"));
        assertEquals("SendGrid", e.getProviderName());
        assertNull(e.getCause());
    }

    @Test
    void providerException_withMessageAndCause() {
        Throwable cause = new RuntimeException("network error");
        ProviderException e = new ProviderException("Twilio", "failed", cause);
        assertTrue(e.getMessage().contains("Twilio"));
        assertTrue(e.getMessage().contains("failed"));
        assertEquals("Twilio", e.getProviderName());
        assertSame(cause, e.getCause());
    }

    @Test
    void validationException_withMessage() {
        ValidationException e = new ValidationException("invalid email");
        assertEquals("invalid email", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void validationException_withMessageAndCause() {
        Throwable cause = new IllegalArgumentException("bad format");
        ValidationException e = new ValidationException("validation failed", cause);
        assertEquals("validation failed", e.getMessage());
        assertSame(cause, e.getCause());
    }
}
