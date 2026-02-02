package com.agora.notification.retry;

import com.agora.notification.models.NotificationResult;

/** 
 * Contract for retry behaviour: when to retry, delay before next attempt, and max attempts. 
 */
public interface RetryPolicy {

    /** Whether to retry after the given result and attempt number (1-based). */
    boolean shouldRetry(NotificationResult result, int attemptNumber);

    /** Delay in ms before the next attempt; attempt number is 1-based (0 before first attempt). */
    long calculateDelayMs(int attemptNumber);

    int getMaxAttempts();
}
