package com.agora.notification.core;

import com.agora.notification.retry.RetryExecutor;

/**
 * Interface for channels that support retry mechanism.
 * This interface allows the Builder to apply retry configuration
 * to channels without knowing their concrete type.
 * 
 */
public interface RetryableChannel extends NotificationChannel {
    
    /**
     * Sets the retry executor for this channel.
     * 
     * @param retryExecutor The retry executor to use
     */
    void setRetryExecutor(RetryExecutor retryExecutor);
}
