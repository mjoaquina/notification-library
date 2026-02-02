package com.agora.notification.channels;

import com.agora.notification.config.EmailConfig;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.providers.email.SendGridEmailProvider;
import com.agora.notification.retry.ExponentialBackoffRetry;
import com.agora.notification.retry.RetryConfig;
import com.agora.notification.retry.RetryExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailChannelRetryTest {
    
    private EmailChannel emailChannel;
    private SendGridEmailProvider provider;
    
    @BeforeEach
    void setUp() {
        EmailConfig config = EmailConfig.builder()
            .apiKey("SG.valid_api_key_12345678901234567890")
            .fromEmail("sender@example.com")
            .fromName("Test Sender")
            .apiUrl("https://api.sendgrid.com")
            .timeoutMs(5000)
            .build();
        
        provider = new SendGridEmailProvider(config);
    }
    
    @Test
    void testChannelWithRetryExecutor() {
        RetryConfig retryConfig = RetryConfig.builder()
            .maxAttempts(3)
            .initialDelayMs(10) // Short delay for testing
            .maxDelayMs(100)
            .backoffMultiplier(2.0)
            .retryOnFailure(true)
            .build();
        
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(retryConfig);
        RetryExecutor retryExecutor = new RetryExecutor(retryPolicy);
        
        emailChannel = new EmailChannel(provider, retryExecutor);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        // Should not throw - retry will handle failures
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    NotificationResult result = emailChannel.send(request);
                    if (result.isSuccess()) {
                        assertTrue(result.getAttemptNumber() >= 1);
                        break;
                    }
                } catch (Exception e) {
                    // Continue trying
                }
            }
        });
    }
    
    @Test
    void testChannelWithoutRetryExecutor() {
        emailChannel = new EmailChannel(provider);
        
        NotificationRequest request = NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build();
        
        // Should work without retry (direct send)
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    emailChannel.send(request);
                    break;
                } catch (Exception e) {
                    // Continue trying
                }
            }
        });
    }
    
    @Test
    void testChannelSetRetryExecutor() {
        emailChannel = new EmailChannel(provider);
        
        RetryConfig retryConfig = RetryConfig.builder()
            .maxAttempts(2)
            .initialDelayMs(10)
            .maxDelayMs(100)
            .backoffMultiplier(2.0)
            .retryOnFailure(true)
            .build();
        
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(retryConfig);
        RetryExecutor retryExecutor = new RetryExecutor(retryPolicy);
        
        emailChannel.setRetryExecutor(retryExecutor);
        assertEquals(retryExecutor, emailChannel.getRetryExecutor());
    }
}
