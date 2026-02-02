package com.agora.notification.core;

import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Main service interface for sending notifications.
 * This is the primary entry point for clients using the library.
 * 
 * Supports both synchronous and asynchronous notification sending,
 * as well as event subscription for notification status updates.
 * 
 * Implements AutoCloseable for proper resource cleanup (e.g. executor shutdown).
 */
public interface NotificationService extends AutoCloseable {
    
    
    NotificationResult send(NotificationRequest request);
    
    
    CompletableFuture<NotificationResult> sendAsync(NotificationRequest request);

    /** Sends multiple notifications asynchronously; future completes with list of results when all are done. If any fails, the future completes exceptionally. */
    CompletableFuture<List<NotificationResult>> sendAsyncBatch(List<NotificationRequest> requests);

    /**
     * Subscribes to notification status events.
     * The consumer will be called whenever a notification status changes.
     * 
     * @param eventConsumer Consumer that receives notification events
     */
    void subscribe(Consumer<NotificationEvent> eventConsumer);
    
    /**
     * Shuts down the service and releases resources (e.g. executor threads).
     * Should be called when the service is no longer needed.
     * Safe to call multiple times.
     */
    void shutdown();
    
    @Override
    default void close() {
        shutdown();
    }
}
