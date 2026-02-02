package com.agora.notification.retry;

import com.agora.notification.core.NotificationProvider;
import com.agora.notification.exceptions.ProviderException;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/** 
 * Runs provider.send() with retries; uses the configured policy for shouldRetry and delay. 
 * Returns the last result or rethrows ProviderException when retries are exhausted. 
 */
@Slf4j
@RequiredArgsConstructor
public class RetryExecutor {

    private final RetryPolicy retryPolicy;

    /** Sends via the provider, retrying on failure until success or max attempts. */
    public NotificationResult executeWithRetry(NotificationProvider provider, NotificationRequest request) {
        int attemptNumber = 1;
        NotificationResult lastResult = null;
        
        while (attemptNumber <= retryPolicy.getMaxAttempts()) {
            try {
                log.debug("Attempt {} of {} to send notification via {}", 
                    attemptNumber, retryPolicy.getMaxAttempts(), provider.getName());
                
                NotificationResult result = provider.send(request);
                
                // Update result with attempt number
                lastResult = NotificationResult.builder()
                    .success(result.isSuccess())
                    .status(result.getStatus())
                    .message(result.getMessage())
                    .providerName(result.getProviderName())
                    .timestamp(result.getTimestamp())
                    .errorDetails(result.getErrorDetails())
                    .attemptNumber(attemptNumber)
                    .build();
                
                // If successful, return immediately
                if (result.isSuccess()) {
                    log.info("Notification sent successfully on attempt {}", attemptNumber);
                    return lastResult;
                }
                
                // Check if we should retry
                if (!retryPolicy.shouldRetry(lastResult, attemptNumber)) {
                    log.warn("Not retrying after attempt {}: max attempts reached or retry disabled", 
                        attemptNumber);
                    return lastResult;
                }
                
                // Calculate delay before next retry
                long delayMs = retryPolicy.calculateDelayMs(attemptNumber + 1);
                if (delayMs > 0) {
                    log.info("Waiting {}ms before retry attempt {}", delayMs, attemptNumber + 1);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Retry delay interrupted");
                        return createFailureResult(provider.getName(), attemptNumber, 
                            "Retry interrupted");
                    }
                }
                
                attemptNumber++;
                
            } catch (ProviderException e) {
                log.warn("Provider exception on attempt {}: {}", attemptNumber, e.getMessage());
                
                lastResult = createFailureResult(provider.getName(), attemptNumber, e.getMessage());
                
                // Check if we should retry
                if (!retryPolicy.shouldRetry(lastResult, attemptNumber)) {
                    log.error("Failed to send notification after {} attempts", attemptNumber);
                    throw e; // Re-throw the last exception
                }
                
                // Calculate delay before next retry
                long delayMs = retryPolicy.calculateDelayMs(attemptNumber + 1);
                if (delayMs > 0) {
                    log.info("Waiting {}ms before retry attempt {} after exception", 
                        delayMs, attemptNumber + 1);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Retry delay interrupted");
                        throw e; // Re-throw original exception
                    }
                }
                
                attemptNumber++;
            }
        }
        
        // If we get here, all attempts failed
        log.error("Failed to send notification after {} attempts", retryPolicy.getMaxAttempts());
        return lastResult != null ? lastResult : 
            createFailureResult(provider.getName(), attemptNumber, "All retry attempts exhausted");
    }
    
    private NotificationResult createFailureResult(String providerName, int attemptNumber, String errorDetails) {
        return NotificationResult.builder()
            .success(false)
            .status(NotificationStatus.FAILED)
            .message("Notification failed after " + attemptNumber + " attempt(s)")
            .providerName(providerName)
            .timestamp(Instant.now())
            .errorDetails(errorDetails)
            .attemptNumber(attemptNumber)
            .build();
    }
}
