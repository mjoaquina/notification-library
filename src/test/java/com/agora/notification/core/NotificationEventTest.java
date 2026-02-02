package com.agora.notification.core;

import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventTest {
    
    @Test
    void pending_withIdChannelRecipient_buildsEventWithExpectedFields() {
        NotificationEvent event = NotificationEvent.pending("test-id", "EMAIL", "test@example.com");
        
        assertEquals("test-id", event.getNotificationId());
        assertEquals(NotificationStatus.PENDING, event.getStatus());
        assertEquals("EMAIL", event.getChannel());
        assertEquals("test@example.com", event.getRecipient());
        assertEquals(0, event.getAttemptNumber());
        assertNotNull(event.getTimestamp());
        assertNull(event.getResult());
    }
    
    @Test
    void retrying_withIdChannelRecipientAttempt_buildsEventWithRetryingStatus() {
        NotificationEvent event = NotificationEvent.retrying("test-id", "SMS", "+1234567890", 2);
        
        assertEquals("test-id", event.getNotificationId());
        assertEquals(NotificationStatus.RETRYING, event.getStatus());
        assertEquals("SMS", event.getChannel());
        assertEquals("+1234567890", event.getRecipient());
        assertEquals(2, event.getAttemptNumber());
        assertNotNull(event.getTimestamp());
    }
    
    @Test
    void sent_withResult_buildsEventWithSentStatusAndResult() {
        NotificationResult result = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        NotificationEvent event = NotificationEvent.sent("test-id", result);
        
        assertEquals("test-id", event.getNotificationId());
        assertEquals(NotificationStatus.SENT, event.getStatus());
        assertEquals(result, event.getResult());
        assertEquals("TestProvider", event.getChannel());
        assertEquals(1, event.getAttemptNumber());
        assertNotNull(event.getTimestamp());
    }
    
    @Test
    void testFailedEvent() {
        NotificationResult result = NotificationResult.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .message("Failed")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .errorDetails("Error details")
            .attemptNumber(3)
            .build();
        
        NotificationEvent event = NotificationEvent.failed("test-id", result);
        
        assertEquals("test-id", event.getNotificationId());
        assertEquals(NotificationStatus.FAILED, event.getStatus());
        assertEquals(result, event.getResult());
        assertEquals("TestProvider", event.getChannel());
        assertEquals(3, event.getAttemptNumber());
        assertNotNull(event.getTimestamp());
    }
    
    @Test
    void builder_withAllFields_buildsCustomEvent() {
        NotificationEvent event = NotificationEvent.builder()
            .notificationId("custom-id")
            .status(NotificationStatus.PENDING)
            .channel("PUSH")
            .recipient("device-token")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        assertEquals("custom-id", event.getNotificationId());
        assertEquals(NotificationStatus.PENDING, event.getStatus());
        assertEquals("PUSH", event.getChannel());
        assertEquals("device-token", event.getRecipient());
        assertEquals(1, event.getAttemptNumber());
    }
}
