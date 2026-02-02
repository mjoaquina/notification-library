package com.agora.notification.retry;

import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ExponentialBackoffRetryTest {
    
    private ExponentialBackoffRetry retryPolicy;
    private RetryConfig config;
    
    @BeforeEach
    void setUp() {
        config = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(1000)
            .maxDelayMs(10000)
            .backoffMultiplier(2.0)
            .retryOnFailure(true)
            .build();
        
        retryPolicy = new ExponentialBackoffRetry(config);
    }
    
    @Test
    void testGetMaxAttempts() {
        assertEquals(3, retryPolicy.getMaxAttempts());
    }
    
    @Test
    void testShouldRetryOnFailure() {
        NotificationResult failureResult = createFailureResult();
        
        assertTrue(retryPolicy.shouldRetry(failureResult, 1));
        assertTrue(retryPolicy.shouldRetry(failureResult, 2));
        assertFalse(retryPolicy.shouldRetry(failureResult, 3)); // Max attempts reached
    }
    
    @Test
    void testShouldNotRetryOnSuccess() {
        NotificationResult successResult = createSuccessResult();
        
        assertFalse(retryPolicy.shouldRetry(successResult, 1));
        assertFalse(retryPolicy.shouldRetry(successResult, 2));
    }
    
    @Test
    void testShouldNotRetryWhenRetryDisabled() {
        RetryConfig noRetryConfig = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(1000)
            .maxDelayMs(10000)
            .backoffMultiplier(2.0)
            .retryOnFailure(false)
            .build();
        
        ExponentialBackoffRetry noRetryPolicy = new ExponentialBackoffRetry(noRetryConfig);
        NotificationResult failureResult = createFailureResult();
        
        assertFalse(noRetryPolicy.shouldRetry(failureResult, 1));
    }
    
    @Test
    void testCalculateDelayForFirstAttempt() {
        long delay = retryPolicy.calculateDelayMs(1);
        assertEquals(0, delay); // No delay before first attempt
    }
    
    @Test
    void testCalculateDelayExponentialBackoff() {
        // Attempt 2: 1000ms (initialDelay * 2^0)
        assertEquals(1000, retryPolicy.calculateDelayMs(2));
        
        // Attempt 3: 2000ms (initialDelay * 2^1)
        assertEquals(2000, retryPolicy.calculateDelayMs(3));
        
        // Attempt 4: 4000ms (initialDelay * 2^2)
        assertEquals(4000, retryPolicy.calculateDelayMs(4));
        
        // Attempt 5: 8000ms (initialDelay * 2^3)
        assertEquals(8000, retryPolicy.calculateDelayMs(5));
    }
    
    @Test
    void testCalculateDelayCappedAtMaxDelay() {
        // With maxDelayMs=10000, attempt 6 should be capped
        // Attempt 6 would be 16000ms without cap, but should be capped at 10000ms
        long delay = retryPolicy.calculateDelayMs(6);
        assertEquals(10000, delay); // Capped at maxDelayMs
    }
    
    @Test
    void testCalculateDelayWithDifferentMultiplier() {
        RetryConfig customConfig = RetryConfig.builder()
            .maxAttempts(5)
            .initialDelayMs(500)
            .maxDelayMs(5000)
            .backoffMultiplier(1.5)
            .retryOnFailure(true)
            .build();
        
        ExponentialBackoffRetry customPolicy = new ExponentialBackoffRetry(customConfig);
        
        // Attempt 2: 500ms (initialDelay * 1.5^0)
        assertEquals(500, customPolicy.calculateDelayMs(2));
        
        // Attempt 3: 750ms (initialDelay * 1.5^1)
        assertEquals(750, customPolicy.calculateDelayMs(3));
        
        // Attempt 4: 1125ms (initialDelay * 1.5^2)
        assertEquals(1125, customPolicy.calculateDelayMs(4));
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
    
    private NotificationResult createFailureResult() {
        return NotificationResult.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .message("Failed")
            .providerName("TestProvider")
            .timestamp(Instant.now())
            .errorDetails("Error details")
            .attemptNumber(1)
            .build();
    }
}
