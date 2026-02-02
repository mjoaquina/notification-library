package com.agora.notification.channels;

import com.agora.notification.core.NotificationProvider;
import com.agora.notification.exceptions.ProviderException;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SmsChannelTest {
    
    @Mock
    private NotificationProvider mockProvider;
    
    private SmsChannel smsChannel;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        smsChannel = new SmsChannel();
    }
    
    @Test
    void testGetChannelType() {
        assertEquals(Channel.SMS, smsChannel.getChannelType());
    }
    
    @Test
    void testSetAndGetProvider() {
        smsChannel.setProvider(mockProvider);
        assertEquals(mockProvider, smsChannel.getProvider());
    }
    
    @Test
    void testSendWithNullProvider() {
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+1234567890")
            .message("Test message")
            .build();
        
        assertThrows(ProviderException.class, () -> smsChannel.send(request));
    }
    
    @Test
    void testSendWithUnconfiguredProvider() {
        when(mockProvider.isConfigured()).thenReturn(false);
        when(mockProvider.getName()).thenReturn("TestProvider");
        
        smsChannel.setProvider(mockProvider);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+1234567890")
            .message("Test message")
            .build();
        
        assertThrows(ProviderException.class, () -> smsChannel.send(request));
        verify(mockProvider).isConfigured();
    }
    
    @Test
    void testSendWithConfiguredProvider() {
        NotificationResult expectedResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("SMS sent")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        when(mockProvider.isConfigured()).thenReturn(true);
        when(mockProvider.send(any(NotificationRequest.class))).thenReturn(expectedResult);
        when(mockProvider.getName()).thenReturn("TestProvider");
        
        smsChannel.setProvider(mockProvider);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+1234567890")
            .message("Test message")
            .build();
        
        NotificationResult result = smsChannel.send(request);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(expectedResult, result);
        verify(mockProvider).send(request);
    }
}
