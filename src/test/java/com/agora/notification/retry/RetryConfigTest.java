package com.agora.notification.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryConfigTest {
    
    @Test
    void testDefaultConfig() {
        RetryConfig config = RetryConfig.defaultConfig();
        
        assertNotNull(config);
        assertEquals(3, config.getMaxAttempts());
        assertEquals(1000, config.getInitialDelayMs());
        assertEquals(10000, config.getMaxDelayMs());
        assertEquals(2.0, config.getBackoffMultiplier());
        assertTrue(config.isRetryOnFailure());
        assertTrue(config.isValid());
    }
    
    @Test
    void testValidConfig() {
        RetryConfig config = RetryConfig.builder()
            .maxAttempts(5)
            .initialDelayMs(500)
            .maxDelayMs(5000)
            .backoffMultiplier(1.5)
            .retryOnFailure(true)
            .build();
        
        assertTrue(config.isValid());
        assertEquals(5, config.getMaxAttempts());
        assertEquals(500, config.getInitialDelayMs());
        assertEquals(5000, config.getMaxDelayMs());
        assertEquals(1.5, config.getBackoffMultiplier());
    }
    
    @Test
    void testInvalidConfigWithZeroMaxAttempts() {
        RetryConfig config = RetryConfig.builder()
            .maxAttempts(0)
            .initialDelayMs(1000)
            .maxDelayMs(10000)
            .backoffMultiplier(2.0)
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testInvalidConfigWithNegativeInitialDelay() {
        RetryConfig config = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(-100)
            .maxDelayMs(10000)
            .backoffMultiplier(2.0)
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testInvalidConfigWithMaxDelayLessThanInitialDelay() {
        RetryConfig config = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(5000)
            .maxDelayMs(1000)
            .backoffMultiplier(2.0)
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testInvalidConfigWithZeroBackoffMultiplier() {
        RetryConfig config = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(1000)
            .maxDelayMs(10000)
            .backoffMultiplier(0)
            .build();
        
        assertFalse(config.isValid());
    }
    
    @Test
    void testConfigWithRetryDisabled() {
        RetryConfig config = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(1000)
            .maxDelayMs(10000)
            .backoffMultiplier(2.0)
            .retryOnFailure(false)
            .build();
        
        assertTrue(config.isValid());
        assertFalse(config.isRetryOnFailure());
    }
}
