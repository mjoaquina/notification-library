package com.agora.notification.retry;

import com.agora.notification.models.NotificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 
 * Retry policy with exponential backoff: delay = initialDelayMs * multiplier^(attempt-2), 
 * capped at maxDelayMs. No delay before first attempt. 
 */
@Slf4j
@RequiredArgsConstructor
public class ExponentialBackoffRetry implements RetryPolicy {
    
    private final RetryConfig config;
    
    @Override
    public boolean shouldRetry(NotificationResult result, int attemptNumber) {
        if (!config.isRetryOnFailure()) {
            return false;
        }
        
        if (attemptNumber >= config.getMaxAttempts()) {
            log.debug("Max attempts ({}) reached, no more retries", config.getMaxAttempts());
            return false;
        }
        
        // Only retry on failures
        if (result.isSuccess()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public long calculateDelayMs(int attemptNumber) {
        if (attemptNumber <= 1) {
            return 0; // No delay before first attempt
        }
        
        // Calculate exponential backoff: initialDelay * (multiplier ^ (attempt - 1))
        long delay = (long) (config.getInitialDelayMs() * 
            Math.pow(config.getBackoffMultiplier(), attemptNumber - 2));
        
        // Cap at maxDelayMs
        long cappedDelay = Math.min(delay, config.getMaxDelayMs());
        
        log.debug("Calculated delay for attempt {}: {}ms (capped from {}ms)", 
            attemptNumber, cappedDelay, delay);
        
        return cappedDelay;
    }
    
    @Override
    public int getMaxAttempts() {
        return config.getMaxAttempts();
    }
}
