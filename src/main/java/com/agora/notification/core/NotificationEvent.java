package com.agora.notification.core;

import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Event published when a notification status changes.
 * Used for Pub/Sub pattern to notify subscribers about notification lifecycle.
 * 
 * Events are published for all status transitions:
 * - PENDING: Notification queued for sending
 * - RETRYING: Notification being retried after failure
 * - SENT: Notification successfully sent
 * - FAILED: Notification failed after all retries
 */
@Getter
@Builder
public class NotificationEvent {
    private final String notificationId;
    private final NotificationStatus status;
    private final NotificationResult result;
    private final String channel;
    private final String recipient;
    private final Instant timestamp;
    private final int attemptNumber;
    
    /**
     * Creates a PENDING event.
     */
    public static NotificationEvent pending(String notificationId, String channel, String recipient) {
        return NotificationEvent.builder()
            .notificationId(notificationId)
            .status(NotificationStatus.PENDING)
            .channel(channel)
            .recipient(recipient)
            .timestamp(Instant.now())
            .attemptNumber(0)
            .build();
    }
    
    public static NotificationEvent retrying(String notificationId, String channel, 
                                             String recipient, int attemptNumber) {
        return NotificationEvent.builder()
            .notificationId(notificationId)
            .status(NotificationStatus.RETRYING)
            .channel(channel)
            .recipient(recipient)
            .timestamp(Instant.now())
            .attemptNumber(attemptNumber)
            .build();
    }
    

    public static NotificationEvent sent(String notificationId, NotificationResult result) {
        return NotificationEvent.builder()
            .notificationId(notificationId)
            .status(NotificationStatus.SENT)
            .result(result)
            .channel(result.getProviderName())
            .recipient(null) // Can be extracted from result if needed
            .timestamp(result.getTimestamp())
            .attemptNumber(result.getAttemptNumber())
            .build();
    }
    
    public static NotificationEvent failed(String notificationId, NotificationResult result) {
        return NotificationEvent.builder()
            .notificationId(notificationId)
            .status(NotificationStatus.FAILED)
            .result(result)
            .channel(result.getProviderName())
            .recipient(null) // Can be extracted from result if needed
            .timestamp(result.getTimestamp())
            .attemptNumber(result.getAttemptNumber())
            .build();
    }
}
