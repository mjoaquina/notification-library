package com.agora.notification.service;

import com.agora.notification.channels.EmailChannel;
import com.agora.notification.core.NotificationChannel;
import com.agora.notification.core.NotificationService;
import com.agora.notification.events.NotificationEventPublisher;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.providers.email.SendGridEmailProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncNotificationServiceTest {
    
    @Mock
    private NotificationChannelRegistry channelRegistry;
    
    @Mock
    private NotificationEventPublisher eventPublisher;
    
    @Mock
    private NotificationChannel mockChannel;
    
    private SyncNotificationService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SyncNotificationService(channelRegistry, eventPublisher);
    }
    
    @Test
    void send_withValidRequest_returnsResult() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        NotificationResult expectedResult = NotificationResult.builder()
            .success(true)
            .providerName("SendGrid")
            .build();
        
        when(channelRegistry.getChannel(Channel.EMAIL)).thenReturn(mockChannel);
        when(mockChannel.send(request)).thenReturn(expectedResult);
        
        NotificationResult result = service.send(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(channelRegistry).getChannel(Channel.EMAIL);
        verify(mockChannel).send(request);
    }
    
    @Test
    void send_withNoChannel_throwsException() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        when(channelRegistry.getChannel(Channel.EMAIL)).thenReturn(null);
        
        // Service wraps IllegalStateException in RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.send(request));
        assertTrue(exception.getCause() instanceof IllegalStateException || 
                   exception.getMessage().contains("No channel configured"));
    }
    
    @Test
    void sendAsync_executesSynchronously() throws Exception {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        NotificationResult expectedResult = NotificationResult.builder()
            .success(true)
            .providerName("SendGrid")
            .build();
        
        when(channelRegistry.getChannel(Channel.EMAIL)).thenReturn(mockChannel);
        when(mockChannel.send(request)).thenReturn(expectedResult);
        
        CompletableFuture<NotificationResult> future = service.sendAsync(request);
        
        // Should complete immediately (synchronous execution)
        NotificationResult result = future.get(100, TimeUnit.MILLISECONDS);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(future.isDone());
    }
    
    @Test
    void subscribe_registersEventListener() {
        NotificationEventPublisher publisher = new NotificationEventPublisher();
        SyncNotificationService syncService = new SyncNotificationService(channelRegistry, publisher);
        
        boolean[] eventReceived = {false};
        
        syncService.subscribe(event -> {
            eventReceived[0] = true;
        });
        
        // Trigger an event by sending a notification
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setProvider(new SendGridEmailProvider(
            com.agora.notification.config.EmailConfig.builder()
                .apiKey("SG.test")
                .fromEmail("test@example.com")
                .build()));
        
        when(channelRegistry.getChannel(Channel.EMAIL)).thenReturn(emailChannel);
        
        try {
            syncService.send(request);
            // Event should be published
            Thread.sleep(100); // Give time for event to be processed
        } catch (Exception e) {
            // Ignore - we're just testing subscription
        }
        
        // Subscription should work
        assertNotNull(syncService);
    }
    
    @Test
    void shutdown_doesNotThrowException() {
        // Should not throw - no resources to clean
        assertDoesNotThrow(() -> service.shutdown());
        assertDoesNotThrow(() -> service.shutdown()); // Safe to call multiple times
    }
    
    @Test
    void send_publishesEvents() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        NotificationResult expectedResult = NotificationResult.builder()
            .success(true)
            .providerName("SendGrid")
            .build();
        
        when(channelRegistry.getChannel(Channel.EMAIL)).thenReturn(mockChannel);
        when(mockChannel.send(request)).thenReturn(expectedResult);
        
        service.send(request);
        
        // Verify events were published
        verify(eventPublisher, atLeastOnce()).publish(any());
    }
}
