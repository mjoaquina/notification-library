package com.agora.notification.models;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Outcome of a send/sendAsync call. success and status reflect delivery; errorDetails set on failure.
 * attemptNumber is 1-based and increases on retries.
 */
@Getter
@Builder
public class NotificationResult {
    private final boolean success;
    private final NotificationStatus status;
    private final String message;
    private final String providerName;
    private final Instant timestamp;
    private final String errorDetails;
    private final int attemptNumber;
}
