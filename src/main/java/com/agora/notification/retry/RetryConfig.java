package com.agora.notification.retry;

import lombok.Builder;
import lombok.Getter;

/**
 * Retry parameters: max attempts, initial/max delay, backoff multiplier, and whether to retry on failure.
 * Valid when maxAttempts > 0, delays >= 0, maxDelayMs >= initialDelayMs, backoffMultiplier > 0.
 */
@Getter
@Builder
public class RetryConfig {
    private final int maxAttempts;
    private final long initialDelayMs;
    private final long maxDelayMs;
    private final double backoffMultiplier;
    private final boolean retryOnFailure;

    /** Default: 3 attempts, 1s initial delay, 10s max delay, 2.0 multiplier, retry on failure. */
    public static RetryConfig defaultConfig() {
        return RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(1000)
            .maxDelayMs(10000)
            .backoffMultiplier(2.0)
            .retryOnFailure(true)
            .build();
    }
    
    public boolean isValid() {
        return maxAttempts > 0 
            && initialDelayMs >= 0
            && maxDelayMs >= initialDelayMs
            && backoffMultiplier > 0;
    }
}
