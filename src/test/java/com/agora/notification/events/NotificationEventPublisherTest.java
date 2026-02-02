package com.agora.notification.events;

import com.agora.notification.core.NotificationEvent;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventPublisherTest {
    
    private NotificationEventPublisher publisher;
    private TestEventListener listener1;
    private TestEventListener listener2;
    
    @BeforeEach
    void setUp() {
        publisher = new NotificationEventPublisher();
        listener1 = new TestEventListener();
        listener2 = new TestEventListener();
    }
    
    @Test
    void testSubscribe() {
        assertEquals(0, publisher.getListenerCount());
        
        publisher.subscribe(listener1);
        assertEquals(1, publisher.getListenerCount());
        
        publisher.subscribe(listener2);
        assertEquals(2, publisher.getListenerCount());
    }
    
    @Test
    void testSubscribeNullListener() {
        publisher.subscribe(null);
        assertEquals(0, publisher.getListenerCount());
    }
    
    @Test
    void testUnsubscribe() {
        publisher.subscribe(listener1);
        publisher.subscribe(listener2);
        assertEquals(2, publisher.getListenerCount());
        
        publisher.unsubscribe(listener1);
        assertEquals(1, publisher.getListenerCount());
        
        publisher.unsubscribe(listener2);
        assertEquals(0, publisher.getListenerCount());
    }
    
    @Test
    void testPublishToSingleListener() {
        publisher.subscribe(listener1);
        
        NotificationEvent event = NotificationEvent.pending("test-id", "EMAIL", "test@example.com");
        publisher.publish(event);
        
        assertEquals(1, listener1.getReceivedEvents().size());
        assertEquals(event, listener1.getReceivedEvents().get(0));
    }
    
    @Test
    void testPublishToMultipleListeners() {
        publisher.subscribe(listener1);
        publisher.subscribe(listener2);
        
        NotificationEvent event = NotificationEvent.pending("test-id", "EMAIL", "test@example.com");
        publisher.publish(event);
        
        assertEquals(1, listener1.getReceivedEvents().size());
        assertEquals(1, listener2.getReceivedEvents().size());
        assertEquals(event, listener1.getReceivedEvents().get(0));
        assertEquals(event, listener2.getReceivedEvents().get(0));
    }
    
    @Test
    void testPublishNullEvent() {
        publisher.subscribe(listener1);
        
        publisher.publish(null);
        
        // Should not throw and should not notify listeners
        assertEquals(0, listener1.getReceivedEvents().size());
    }
    
    @Test
    void testPublishMultipleEvents() {
        publisher.subscribe(listener1);
        
        NotificationEvent event1 = NotificationEvent.pending("id1", "EMAIL", "test1@example.com");
        NotificationEvent event2 = NotificationEvent.sent("id2", createSuccessResult());
        
        publisher.publish(event1);
        publisher.publish(event2);
        
        assertEquals(2, listener1.getReceivedEvents().size());
        assertEquals(event1, listener1.getReceivedEvents().get(0));
        assertEquals(event2, listener1.getReceivedEvents().get(1));
    }
    
    @Test
    void testListenerExceptionHandling() {
        NotificationEventListener failingListener = event -> {
            throw new RuntimeException("Listener error");
        };
        
        publisher.subscribe(failingListener);
        publisher.subscribe(listener1);
        
        NotificationEvent event = NotificationEvent.pending("test-id", "EMAIL", "test@example.com");
        
        // Should not throw even if one listener fails
        assertDoesNotThrow(() -> publisher.publish(event));
        
        // Other listeners should still receive the event
        assertEquals(1, listener1.getReceivedEvents().size());
    }
    
    @Test
    void testClear() {
        publisher.subscribe(listener1);
        publisher.subscribe(listener2);
        assertEquals(2, publisher.getListenerCount());
        
        publisher.clear();
        assertEquals(0, publisher.getListenerCount());
    }
    
    private NotificationResult createSuccessResult() {
        return NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
    }
    
    private static class TestEventListener implements NotificationEventListener {
        private final List<NotificationEvent> receivedEvents = new ArrayList<>();
        
        @Override
        public void onEvent(NotificationEvent event) {
            receivedEvents.add(event);
        }
        
        public List<NotificationEvent> getReceivedEvents() {
            return receivedEvents;
        }
    }
}
