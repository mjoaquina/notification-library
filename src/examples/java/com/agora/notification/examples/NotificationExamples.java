package com.agora.notification.examples;

import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.models.NotificationStatus;
import com.agora.notification.retry.RetryConfig;
import com.agora.notification.service.NotificationServiceBuilder;
import com.agora.notification.core.NotificationService;
import com.agora.notification.factory.ChannelFactory;
import com.agora.notification.core.NotificationChannel;

import java.util.concurrent.CompletableFuture;

/**
 * Complete examples demonstrating how to use the notification library.
 *
 * Some examples use {@link StubNotificationChannel} for deterministic simulated success
 * (no real API calls); others use {@link ChannelFactory} with simulated providers,
 * which may succeed or fail. This demonstrates both success and error handling
 * as allowed by the requirements (simulate sending, return simulated results).
 *
 * Usage patterns:
 * - Basic synchronous sending (Example 1: stub → success)
 * - Asynchronous sending with CompletableFuture (Example 2: simulated provider)
 * - Event subscription / Pub/Sub (Example 3: stub → SENT events)
 * - Retry configuration (Example 4: simulated provider, may retry)
 * - Builder pattern (Example 5: configuration only)
 * - Direct channel usage (Example 6: simulated provider)
 */
public class NotificationExamples {
    
    public static void main(String[] args) {
        System.out.println("=== Notification Library Examples ===\n");
        
        example1_BasicUsage();
        example2_AsyncUsage();
        example3_EventSubscription();
        example4_WithRetry();
        example5_BuilderPattern();
        example6_DirectChannelUsage();
    }
    
    /**
     * Example 1: Basic synchronous notification sending (simulated success via stub)
     */
    public static void example1_BasicUsage() {
        System.out.println("Example 1: Basic Synchronous Sending");
        System.out.println("----------------------------------------");
        
        // Use stub channel for deterministic simulated success (no real API calls)
        NotificationChannel emailChannel = new StubNotificationChannel(Channel.EMAIL);
        
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(emailChannel)
                .build()) {
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.EMAIL)
                .recipient("user@example.com")
                .subject("Welcome!")
                .message("Hello, welcome to our service!")
                .build();
            
            try {
                NotificationResult result = service.send(request);
                
                if (result.isSuccess()) {
                    System.out.println("✓ Email sent successfully!");
                    System.out.println("  Provider: " + result.getProviderName());
                    System.out.println("  Attempts: " + result.getAttemptNumber());
                } else {
                    System.out.println("✗ Failed to send email");
                    System.out.println("  Error: " + result.getErrorDetails());
                }
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("✗ Error closing service: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Example 2: Asynchronous notification sending
     */
    public static void example2_AsyncUsage() {
        System.out.println("Example 2: Asynchronous Sending");
        System.out.println("--------------------------------");
        
        // Create SMS channel using factory
        NotificationChannel smsChannel = ChannelFactory.createChannel(
            Channel.SMS, "Twilio",
            "AC1234567890",
            "auth-token-here",
            "+1234567890"
        );
        
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(smsChannel)
                .build()) {
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.SMS)
                .recipient("+0987654321")
                .message("Your verification code is: 123456")
                .build();
            
            CompletableFuture<NotificationResult> future = service.sendAsync(request);
            
            System.out.println("Request submitted, processing asynchronously...");
            
            CompletableFuture<Void> handled = future.thenAccept(result -> {
                if (result.isSuccess()) {
                    System.out.println("✓ SMS sent asynchronously!");
                    System.out.println("  Status: " + result.getStatus());
                } else {
                    System.out.println("✗ SMS failed: " + result.getErrorDetails());
                }
            }).exceptionally(throwable -> {
                System.out.println("✗ Exception: " + (throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage()));
                return null;
            });
            
            try {
                handled.join();
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("✗ Error closing service: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Example 3: Event subscription (Pub/Sub pattern, simulated success via stub)
     */
    public static void example3_EventSubscription() {
        System.out.println("Example 3: Event Subscription");
        System.out.println("------------------------------");
        
        // Use stub channel so events show SENT and success message
        NotificationChannel pushChannel = new StubNotificationChannel(Channel.PUSH);
        
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(pushChannel)
                .build()) {
            
            service.subscribe(event -> {
                System.out.println("Event received:");
                System.out.println("  ID: " + event.getNotificationId());
                System.out.println("  Status: " + event.getStatus());
                System.out.println("  Channel: " + event.getChannel());
                System.out.println("  Timestamp: " + event.getTimestamp());
                
                if (event.getStatus() == NotificationStatus.SENT) {
                    System.out.println("  ✓ Notification sent successfully!");
                } else if (event.getStatus() == NotificationStatus.FAILED) {
                    System.out.println("  ✗ Notification failed");
                }
            });
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.PUSH)
                .recipient("device-token-12345")
                .title("New Message")
                .body("You have a new message")
                .message("Check your inbox")
                .build();
            
            try {
                service.send(request);
            } catch (Exception e) {
                System.out.println("✗ Error sending push notification: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("✗ Error closing service: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Example 4: Using retry configuration
     */
    public static void example4_WithRetry() {
        System.out.println("Example 4: With Retry Configuration");
        System.out.println("-----------------------------------");
        
        // Create email channel using factory
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "Mailgun",
            "key-your-mailgun-key",
            "sender@example.com",
            "Your App"
        );
        
        RetryConfig retryConfig = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(1000)      // 1 second initial delay
            .maxDelayMs(10000)          // Max 10 seconds delay
            .backoffMultiplier(2.0)     // Double delay each retry
            .retryOnFailure(true)
            .build();
        
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(emailChannel)
                .retryConfig(retryConfig)
                .build()) {
            
            NotificationRequest request = NotificationRequest.builder()
                .channel(Channel.EMAIL)
                .recipient("user@example.com")
                .subject("Important Update")
                .message("This is an important message that will be retried if it fails.")
                .build();
            
            try {
                NotificationResult result = service.send(request);
                System.out.println("Final result after retries:");
                System.out.println("  Success: " + result.isSuccess());
                System.out.println("  Attempts: " + result.getAttemptNumber());
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("✗ Error closing service: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Example 5: Using Builder pattern for complete configuration
     */
    public static void example5_BuilderPattern() {
        System.out.println("Example 5: Complete Builder Configuration");
        System.out.println("----------------------------------------");
        
        // Create all channels using factory
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid",
            "SG.api-key",
            "noreply@example.com",
            "My Application"
        );
        
        NotificationChannel smsChannel = ChannelFactory.createChannel(
            Channel.SMS, "Twilio",
            "AC123",
            "auth-token",
            "+1234567890"
        );
        
        NotificationChannel pushChannel = ChannelFactory.createChannel(
            Channel.PUSH, "OneSignal",
            "api-key",
            "app-id"
        );
        
        try (NotificationService service = NotificationServiceBuilder.builder()
                .registerChannel(emailChannel)
                .registerChannel(smsChannel)
                .registerChannel(pushChannel)
                .retryConfig(RetryConfig.builder()
                    .maxAttempts(3)
                    .initialDelayMs(1000)
                    .maxDelayMs(10000)
                    .backoffMultiplier(2.0)
                    .build())
                .build()) {
            
            System.out.println("Service configured with:");
            System.out.println("  - Email: SendGrid");
            System.out.println("  - SMS: Twilio");
            System.out.println("  - Push: OneSignal");
            System.out.println("  - Retry: 3 attempts with exponential backoff");
        } catch (Exception e) {
            System.out.println("✗ Error closing service: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Example 6: Direct channel usage (advanced) — simulated success via stub
     */
    public static void example6_DirectChannelUsage() {
        System.out.println("Example 6: Direct Channel Usage");
        System.out.println("------------------------------");
        
        // Stub channel for deterministic success when calling send() directly
        NotificationChannel emailChannel = new StubNotificationChannel(Channel.EMAIL);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Direct Channel Usage")
            .message("This email was sent using the channel directly.")
            .build();
        
        try {
            NotificationResult result = emailChannel.send(request);
            System.out.println("Direct channel result:");
            System.out.println("  Success: " + result.isSuccess());
            System.out.println("  Provider: " + result.getProviderName());
            if (result.isSuccess()) {
                System.out.println("  ✓ Sent successfully (simulated)");
            }
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
        System.out.println();
    }
}
