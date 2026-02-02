package com.agora.notification.service;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.core.NotificationService;
import com.agora.notification.core.RetryableChannel;
import com.agora.notification.events.NotificationEventPublisher;
import com.agora.notification.retry.ExponentialBackoffRetry;
import com.agora.notification.retry.RetryConfig;
import com.agora.notification.retry.RetryExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating NotificationService instances.
 * Provides fluent API for configuring all aspects of the notification service.
 * 
 * It supports configuring retry mechanisms for channels that implement the RetryableChannel interface.
 */
@Slf4j
public class NotificationServiceBuilder {
    
    // Extensible list of registered channels
    private final List<NotificationChannel> registeredChannels = new ArrayList<>();

    // Retry configuration (applied to all channels that support retry)
    private RetryConfig retryConfig;
    
    // Service execution mode (default: ASYNC for backward compatibility)
    private ExecutionMode executionMode = ExecutionMode.ASYNC;
    
    /**
     * Execution mode for the notification service.
     */
    public enum ExecutionMode {
        SYNC,
        ASYNC
    }
    
    private NotificationServiceBuilder() {}
    
    public static NotificationServiceBuilder builder() {
        return new NotificationServiceBuilder();
    }
    
    /**
     * Registers a channel directly.
     * This is the extensible way to add channels.
     * 
     * @param channel The channel to register
     * @return This builder for method chaining
     */
    public NotificationServiceBuilder registerChannel(NotificationChannel channel) {
        if (channel != null) {
            registeredChannels.add(channel);
            log.debug("Registered channel: {}", channel.getChannelType());
        }
        return this;
    }
    
    /**
     * Sets the retry configuration to be applied to all channels that support retry.
     * 
     * @param config The retry configuration
     * @return This builder for method chaining
     */
    public NotificationServiceBuilder retryConfig(RetryConfig config) {
        this.retryConfig = config;
        return this;
    }

    public NotificationServiceBuilder executionMode(ExecutionMode mode) {
        if (mode != null) {
            this.executionMode = mode;
            log.debug("Set execution mode: {}", mode);
        }
        return this;
    }

    /** send() blocks until done; no thread pool. */
    public NotificationServiceBuilder sync() {
        this.executionMode = ExecutionMode.SYNC;
        return this;
    }

    /** sendAsync() runs in thread pool; default mode. */
    public NotificationServiceBuilder async() {
        this.executionMode = ExecutionMode.ASYNC;
        return this;
    }

    /** Builds the service, registers channels, applies retry to RetryableChannel instances. */
    public NotificationService build() {
        NotificationChannelRegistry registry = new NotificationChannelRegistry();
        NotificationEventPublisher publisher = new NotificationEventPublisher();
        
        // Create the appropriate service based on execution mode
        NotificationService service;
        if (executionMode == ExecutionMode.SYNC) {
            service = new SyncNotificationService(registry, publisher);
            log.debug("Building SyncNotificationService");
        } else {
            service = new AsyncNotificationService(registry, publisher);
            log.debug("Building AsyncNotificationService");
        }
        
        // Create retry executor if configured
        RetryExecutor retryExecutor = null;
        if (retryConfig != null && retryConfig.isValid()) {
            ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(retryConfig);
            retryExecutor = new RetryExecutor(retryPolicy);
        }
        
        // Register all channels and apply retry configuration
        for (NotificationChannel channel : registeredChannels) {
            applyRetryExecutor(channel, retryExecutor);
            registry.register(channel);
        }
        
        return service;
    }

    private void applyRetryExecutor(NotificationChannel channel, RetryExecutor retryExecutor) {
        if (retryExecutor != null && channel instanceof RetryableChannel) {
            ((RetryableChannel) channel).setRetryExecutor(retryExecutor);
        }
    }
}
