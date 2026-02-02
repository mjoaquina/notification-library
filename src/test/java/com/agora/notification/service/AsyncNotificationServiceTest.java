package com.agora.notification.service;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.core.NotificationEvent;
import com.agora.notification.events.NotificationEventPublisher;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AsyncNotificationServiceTest {
    
    @Mock
    private NotificationChannel mockChannel;
    
    private NotificationChannelRegistry channelRegistry;
    private NotificationEventPublisher eventPublisher;
    private AsyncNotificationService service;
    private List<NotificationEvent> receivedEvents;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        channelRegistry = new NotificationChannelRegistry();
        eventPublisher = new NotificationEventPublisher();
        service = new AsyncNotificationService(channelRegistry, eventPublisher);
        receivedEvents = new ArrayList<>();
        
        // Subscribe to events
        service.subscribe(receivedEvents::add);
        
        // Setup mock channel
        when(mockChannel.getChannelType()).thenReturn(Channel.EMAIL);
        channelRegistry.register(mockChannel);
    }
    
    @Test
    void send_withSuccessResult_returnsResultAndPublishesPendingThenSent() {
        // Arrange
        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();

        when(mockChannel.send(any(NotificationRequest.class))).thenReturn(successResult);

        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        // Act
        NotificationResult result = service.send(request);

        // Assert
        assertTrue(result.isSuccess());
        verify(mockChannel).send(request);
        assertEquals(2, receivedEvents.size());
        assertEquals(NotificationStatus.PENDING, receivedEvents.get(0).getStatus());
        assertEquals(NotificationStatus.SENT, receivedEvents.get(1).getStatus());
    }
    
    @Test
    void testSendAsync() throws Exception {
        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        when(mockChannel.send(any(NotificationRequest.class))).thenReturn(successResult);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        CompletableFuture<NotificationResult> future = service.sendAsync(request);
        
        // Wait for completion
        NotificationResult result = future.get(2, TimeUnit.SECONDS);
        
        assertTrue(result.isSuccess());
        verify(mockChannel).send(request);
        
        // Should have published PENDING and SENT events
        assertEquals(2, receivedEvents.size());
        assertEquals(NotificationStatus.PENDING, receivedEvents.get(0).getStatus());
        assertEquals(NotificationStatus.SENT, receivedEvents.get(1).getStatus());
    }
    
    @Test
    void testSendAsyncFailure() throws Exception {
        NotificationResult failureResult = NotificationResult.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .message("Failed")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        when(mockChannel.send(any(NotificationRequest.class))).thenReturn(failureResult);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        CompletableFuture<NotificationResult> future = service.sendAsync(request);
        
        // Wait for completion
        NotificationResult result = future.get(2, TimeUnit.SECONDS);
        
        assertFalse(result.isSuccess());
        
        // Should have published PENDING and FAILED events
        assertEquals(2, receivedEvents.size());
        assertEquals(NotificationStatus.PENDING, receivedEvents.get(0).getStatus());
        assertEquals(NotificationStatus.FAILED, receivedEvents.get(1).getStatus());
    }
    
    @Test
    void testSendWithException() {
        when(mockChannel.send(any(NotificationRequest.class)))
            .thenThrow(new RuntimeException("Channel error"));
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        assertThrows(RuntimeException.class, () -> service.send(request));
        
        // Should have published PENDING and FAILED events
        assertEquals(2, receivedEvents.size());
        assertEquals(NotificationStatus.PENDING, receivedEvents.get(0).getStatus());
        assertEquals(NotificationStatus.FAILED, receivedEvents.get(1).getStatus());
    }
    
    @Test
    void testSendWithNoChannel() {
        channelRegistry.clear();
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        // Service wraps IllegalStateException in RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.send(request));
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertTrue(exception.getMessage().contains("Failed to send notification"));
    }
    
    @Test
    void testSubscribe() {
        List<NotificationEvent> customEvents = new ArrayList<>();
        service.subscribe(customEvents::add);
        
        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        when(mockChannel.send(any(NotificationRequest.class))).thenReturn(successResult);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        service.send(request);
        
        // Both subscribers should receive events
        assertEquals(2, receivedEvents.size());
        assertEquals(2, customEvents.size());
    }
    
    @Test
    void testShutdown() {
        service.shutdown();
        assertDoesNotThrow(() -> service.shutdown());
    }

    @Test
    void testSendWithFailedResult_publishesFailedEvent() {
        NotificationResult failureResult = NotificationResult.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .message("Failed")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();

        when(mockChannel.send(any(NotificationRequest.class))).thenReturn(failureResult);

        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        NotificationResult result = service.send(request);

        assertFalse(result.isSuccess());
        assertEquals(2, receivedEvents.size());
        assertEquals(NotificationStatus.PENDING, receivedEvents.get(0).getStatus());
        assertEquals(NotificationStatus.FAILED, receivedEvents.get(1).getStatus());
    }

    @Test
    void testSendAsyncWithException_publishesFailedAndThrows() throws Exception {
        when(mockChannel.send(any(NotificationRequest.class)))
            .thenThrow(new RuntimeException("Async channel error"));

        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        CompletableFuture<NotificationResult> future = service.sendAsync(request);

        assertThrows(Exception.class, () -> future.get(2, TimeUnit.SECONDS));
        assertEquals(2, receivedEvents.size());
        assertEquals(NotificationStatus.PENDING, receivedEvents.get(0).getStatus());
        assertEquals(NotificationStatus.FAILED, receivedEvents.get(1).getStatus());
    }

    @Test
    void testSubscribeWhenConsumerThrows_doesNotPropagate() {
        List<NotificationEvent> customEvents = new ArrayList<>();
        service.subscribe(ev -> {
            customEvents.add(ev);
            throw new RuntimeException("Consumer error");
        });

        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        when(mockChannel.send(any(NotificationRequest.class))).thenReturn(successResult);

        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();

        assertDoesNotThrow(() -> service.send(request));
        assertEquals(2, customEvents.size());
    }

    @Test
    void testClose_callsShutdown() {
        assertDoesNotThrow(() -> service.close());
        assertDoesNotThrow(() -> service.close());
    }
}
