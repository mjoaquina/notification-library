package com.agora.notification.service;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.core.NotificationEvent;
import com.agora.notification.core.NotificationService;
import com.agora.notification.events.NotificationEventPublisher;
import com.agora.notification.events.NotificationEventListener;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** 
 * Notification service that runs send() in a thread pool; sendAsync() returns a CompletableFuture. 
 * Publishes PENDING/SENT/FAILED events. Call shutdown() to release the executor. 
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncNotificationService implements NotificationService {
    
    private final NotificationChannelRegistry channelRegistry;
    private final NotificationEventPublisher eventPublisher;
    private final ExecutorService executorService;
    
    public AsyncNotificationService(NotificationChannelRegistry channelRegistry, 
                                   NotificationEventPublisher eventPublisher) {
        this.channelRegistry = channelRegistry;
        this.eventPublisher = eventPublisher;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    @Override
    public NotificationResult send(NotificationRequest request) {
        String notificationId = generateNotificationId();
        Channel channel = request.getChannel();
        
        // Publish PENDING event
        publishEvent(NotificationEvent.pending(notificationId, channel.name(), request.getRecipient()));
        
        try {
            NotificationChannel notificationChannel = channelRegistry.getChannel(channel);
            if (notificationChannel == null) {
                throw new IllegalStateException("No channel configured for: " + channel);
            }
            
            NotificationResult result = notificationChannel.send(request);
            
            // Publish result event
            if (result.isSuccess()) {
                publishEvent(NotificationEvent.sent(notificationId, result));
            } else {
                publishEvent(NotificationEvent.failed(notificationId, result));
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error sending notification {}: {}", notificationId, e.getMessage(), e);
            
            // Publish FAILED event
            NotificationResult failureResult = NotificationResult.builder()
                .success(false)
                .status(com.agora.notification.models.NotificationStatus.FAILED)
                .message("Error: " + e.getMessage())
                .providerName(channel.name())
                .timestamp(java.time.Instant.now())
                .errorDetails(e.getMessage())
                .attemptNumber(1)
                .build();
            
            publishEvent(NotificationEvent.failed(notificationId, failureResult));
            throw new RuntimeException("Failed to send notification", e);
        }
    }
    
    @Override
    public CompletableFuture<NotificationResult> sendAsync(NotificationRequest request) {
        String notificationId = generateNotificationId();
        Channel channel = request.getChannel();
        
        // Publish PENDING event immediately
        publishEvent(NotificationEvent.pending(notificationId, channel.name(), request.getRecipient()));
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                NotificationChannel notificationChannel = channelRegistry.getChannel(channel);
                if (notificationChannel == null) {
                    throw new IllegalStateException("No channel configured for: " + channel);
                }
                
                NotificationResult result = notificationChannel.send(request);
                
                // Publish result event
                if (result.isSuccess()) {
                    publishEvent(NotificationEvent.sent(notificationId, result));
                } else {
                    publishEvent(NotificationEvent.failed(notificationId, result));
                }
                
                return result;
            } catch (Exception e) {
                log.error("Error sending notification {} asynchronously: {}", notificationId, e.getMessage(), e);
                
                // Publish FAILED event
                NotificationResult failureResult = NotificationResult.builder()
                    .success(false)
                    .status(com.agora.notification.models.NotificationStatus.FAILED)
                    .message("Error: " + e.getMessage())
                    .providerName(channel.name())
                    .timestamp(java.time.Instant.now())
                    .errorDetails(e.getMessage())
                    .attemptNumber(1)
                    .build();
                
                publishEvent(NotificationEvent.failed(notificationId, failureResult));
                throw new RuntimeException("Failed to send notification asynchronously", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<List<NotificationResult>> sendAsyncBatch(List<NotificationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        List<CompletableFuture<NotificationResult>> futures = requests.stream()
                .map(this::sendAsync)
                .collect(Collectors.toList());
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    @Override
    public void subscribe(Consumer<NotificationEvent> eventConsumer) {
        eventPublisher.subscribe(new NotificationEventListener() {
            @Override
            public void onEvent(NotificationEvent event) {
                try {
                    eventConsumer.accept(event);
                } catch (Exception e) {
                    log.error("Error in event consumer: {}", e.getMessage(), e);
                }
            }
        });
    }
    
    @Override
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                        log.warn("Executor did not terminate in time");
                    }
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("Shutdown interrupted", e);
            }
            log.debug("AsyncNotificationService executor shutdown");
        }
    }
    
    private void publishEvent(NotificationEvent event) {
        if (eventPublisher != null) {
            eventPublisher.publish(event);
        }
    }
    
    private String generateNotificationId() {
        return UUID.randomUUID().toString();
    }
}
