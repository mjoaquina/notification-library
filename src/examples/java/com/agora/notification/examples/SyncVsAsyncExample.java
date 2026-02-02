package com.agora.notification.examples;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.core.NotificationService;
import com.agora.notification.factory.ChannelFactory;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.service.NotificationServiceBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Example demonstrating the difference between synchronous and asynchronous
 * notification services.
 */
public class SyncVsAsyncExample {
    
    public static void main(String[] args) {
        System.out.println("=== Sync vs Async Notification Service ===\n");
        
        example1_SyncService();
        example2_AsyncService();
        example3_BuilderSyncMode();
        example4_BuilderAsyncMode();
    }
    
    /**
     * Example 1: Using synchronous service (default with .sync())
     */
    public static void example1_SyncService() {
        System.out.println("Example 1: Synchronous Service");
        System.out.println("--------------------------------");
        
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid",
            "SG.test-key",
            "sender@example.com",
            "Test Sender"
        );
        
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(emailChannel)
                .sync()  // ← Explicitly use sync mode
                .build()) {
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.EMAIL)
                .recipient("user@example.com")
                .subject("Sync Test")
                .message("This is sent synchronously")
                .build();
            
            System.out.println("Sending synchronously (will block)...");
            long startTime = System.currentTimeMillis();
            
            NotificationResult result = service.send(request);
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("✓ Completed in " + duration + "ms");
            System.out.println("  Success: " + result.isSuccess());
            System.out.println("  Note: sendAsync() also executes synchronously in sync mode\n");
        }
    }
    
    /**
     * Example 2: Using asynchronous service (default)
     */
    public static void example2_AsyncService() {
        System.out.println("Example 2: Asynchronous Service");
        System.out.println("--------------------------------");
        
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid",
            "SG.test-key",
            "sender@example.com",
            "Test Sender"
        );
        
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(emailChannel)
                .async()  // ← Explicitly use async mode (this is the default)
                .build()) {
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.EMAIL)
                .recipient("user@example.com")
                .subject("Async Test")
                .message("This is sent asynchronously")
                .build();
            
            System.out.println("Sending asynchronously (won't block)...");
            long startTime = System.currentTimeMillis();
            
            CompletableFuture<NotificationResult> future = service.sendAsync(request);
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("✓ Future returned immediately in " + duration + "ms");
            System.out.println("  Future is done: " + future.isDone());
            
            // Wait for completion
            try {
                NotificationResult result = future.get();
                System.out.println("  Final result - Success: " + result.isSuccess());
            } catch (Exception e) {
                System.out.println("  Error: " + e.getMessage());
            }
            System.out.println();
        }
    }
    
    /**
     * Example 3: Builder with sync mode (lightweight, no thread pool)
     */
    public static void example3_BuilderSyncMode() {
        System.out.println("Example 3: Builder - Sync Mode");
        System.out.println("------------------------------");
        
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid",
            "SG.test-key",
            "sender@example.com",
            "Test Sender"
        );
        
        // Sync mode: lightweight, no thread pool overhead
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(emailChannel)
                .sync()  // ← Sync mode
                .build()) {
            
            System.out.println("Service type: SyncNotificationService");
            System.out.println("Characteristics:");
            System.out.println("  - No thread pool");
            System.out.println("  - Blocks on send()");
            System.out.println("  - sendAsync() executes synchronously");
            System.out.println("  - Lightweight, suitable for simple use cases");
            System.out.println();
        }
    }
    
    /**
     * Example 4: Builder with async mode (default, with thread pool)
     */
    public static void example4_BuilderAsyncMode() {
        System.out.println("Example 4: Builder - Async Mode");
        System.out.println("-------------------------------");
        
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid",
            "SG.test-key",
            "sender@example.com",
            "Test Sender"
        );
        
        // Async mode: uses thread pool for async operations
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(emailChannel)
                .async()  // ← Async mode (default)
                .build()) {
            
            System.out.println("Service type: AsyncNotificationService");
            System.out.println("Characteristics:");
            System.out.println("  - Uses thread pool (ExecutorService)");
            System.out.println("  - Blocks on send()");
            System.out.println("  - sendAsync() executes in thread pool");
            System.out.println("  - Suitable for high-throughput scenarios");
            System.out.println();
        }
    }
}
