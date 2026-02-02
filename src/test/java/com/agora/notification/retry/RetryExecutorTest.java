package com.agora.notification.retry;

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

class RetryExecutorTest {
    
    @Mock
    private NotificationProvider mockProvider;
    
    private RetryExecutor retryExecutor;
    private RetryConfig retryConfig;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        retryConfig = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(10) // Short delay for testing
            .maxDelayMs(100)
            .backoffMultiplier(2.0)
            .retryOnFailure(true)
            .build();
        
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(retryConfig);
        retryExecutor = new RetryExecutor(retryPolicy);
        
        when(mockProvider.getName()).thenReturn("TestProvider");
    }
    
    @Test
    void testExecuteWithRetrySuccessOnFirstAttempt() {
        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        when(mockProvider.send(any(NotificationRequest.class))).thenReturn(successResult);
        
        NotificationRequest request = createRequest();
        NotificationResult result = retryExecutor.executeWithRetry(mockProvider, request);
        
        assertTrue(result.isSuccess());
        assertEquals(1, result.getAttemptNumber());
        verify(mockProvider, times(1)).send(request);
    }
    
    @Test
    void testExecuteWithRetrySuccessOnSecondAttempt() {
        NotificationResult failureResult = NotificationResult.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .message("Failed")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(2)
            .build();
        
        when(mockProvider.send(any(NotificationRequest.class)))
            .thenReturn(failureResult)
            .thenReturn(successResult);
        
        NotificationRequest request = createRequest();
        NotificationResult result = retryExecutor.executeWithRetry(mockProvider, request);
        
        assertTrue(result.isSuccess());
        assertEquals(2, result.getAttemptNumber());
        verify(mockProvider, times(2)).send(request);
    }
    
    @Test
    void testExecuteWithRetryAllAttemptsFail() {
        NotificationResult failureResult = NotificationResult.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .message("Failed")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        when(mockProvider.send(any(NotificationRequest.class))).thenReturn(failureResult);
        
        NotificationRequest request = createRequest();
        NotificationResult result = retryExecutor.executeWithRetry(mockProvider, request);
        
        assertFalse(result.isSuccess());
        assertEquals(3, result.getAttemptNumber());
        verify(mockProvider, times(3)).send(request);
    }
    
    @Test
    void testExecuteWithRetryProviderException() {
        when(mockProvider.send(any(NotificationRequest.class)))
            .thenThrow(new ProviderException("TestProvider", "Provider error"));
        
        NotificationRequest request = createRequest();
        
        assertThrows(ProviderException.class, () -> {
            retryExecutor.executeWithRetry(mockProvider, request);
        });
        
        verify(mockProvider, times(3)).send(request); // Max attempts
    }
    
    @Test
    void testExecuteWithRetryProviderExceptionThenSuccess() {
        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(2)
            .build();
        
        when(mockProvider.send(any(NotificationRequest.class)))
            .thenThrow(new ProviderException("TestProvider", "Provider error"))
            .thenReturn(successResult);
        
        NotificationRequest request = createRequest();
        NotificationResult result = retryExecutor.executeWithRetry(mockProvider, request);
        
        assertTrue(result.isSuccess());
        assertEquals(2, result.getAttemptNumber());
        verify(mockProvider, times(2)).send(request);
    }
    
    @Test
    void testExecuteWithRetryNoRetryOnSuccess() {
        NotificationResult successResult = NotificationResult.builder()
            .success(true)
            .status(NotificationStatus.SENT)
            .message("Success")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .attemptNumber(1)
            .build();
        
        when(mockProvider.send(any(NotificationRequest.class))).thenReturn(successResult);
        
        NotificationRequest request = createRequest();
        NotificationResult result = retryExecutor.executeWithRetry(mockProvider, request);
        
        assertTrue(result.isSuccess());
        verify(mockProvider, times(1)).send(request); // Only one attempt
    }
    
    private NotificationRequest createRequest() {
        return NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("test@example.com")
            .subject("Test")
            .message("Test message")
            .build();
    }
}
