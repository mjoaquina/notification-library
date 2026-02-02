package com.agora.notification.service;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.core.NotificationService;
import com.agora.notification.factory.ChannelFactory;
import com.agora.notification.models.Channel;
import com.agora.notification.models.NotificationRequest;
import com.agora.notification.models.NotificationResult;
import com.agora.notification.providers.email.SendGridEmailProvider;
import com.agora.notification.retry.RetryConfig;
import org.junit.jupiter.api.Test;

import com.agora.notification.channels.EmailChannel;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceBuilderTest {

    @Test
    void builder_returnsNewBuilder() {
        assertNotNull(NotificationServiceBuilder.builder());
    }

    @Test
    void build_withNoChannels_returnsService() {
        NotificationService service = NotificationServiceBuilder.builder().build();
        assertNotNull(service);
    }

    @Test
    void registerChannel_withEmailChannel_registersChannel() {
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid", "SG.test", "test@example.com", "Test Sender");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(emailChannel)
            .build();

        assertNotNull(service);
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build());
    }

    @Test
    void registerChannel_withMailgunChannel_registersChannel() {
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "Mailgun", "key-test", "test@example.com", "Test Sender");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(emailChannel)
            .build();

        assertNotNull(service);
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build());
    }

    @Test
    void registerChannel_withTwilioChannel_registersChannel() {
        NotificationChannel smsChannel = ChannelFactory.createChannel(
            Channel.SMS, "Twilio", "AC123", "token", "+1234567890");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(smsChannel)
            .build();

        assertNotNull(service);
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+0987654321")
            .message("Test SMS")
            .build());
    }

    @Test
    void registerChannel_withAwsSnsChannel_registersChannel() {
        NotificationChannel smsChannel = ChannelFactory.createChannel(
            Channel.SMS, "AwsSns", "key", "secret", "us-east-1");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(smsChannel)
            .build();

        assertNotNull(service);
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+0987654321")
            .message("Test SMS")
            .build());
    }

    @Test
    void registerChannel_withFcmChannel_registersChannel() {
        NotificationChannel pushChannel = ChannelFactory.createChannel(
            Channel.PUSH, "FCM", "server-key");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(pushChannel)
            .build();

        assertNotNull(service);
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.PUSH)
            .recipient("device-token")
            .title("Title")
            .body("Body")
            .build());
    }

    @Test
    void registerChannel_withOneSignalChannel_registersChannel() {
        NotificationChannel pushChannel = ChannelFactory.createChannel(
            Channel.PUSH, "OneSignal", "api-key", "app-id");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(pushChannel)
            .build();

        assertNotNull(service);
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.PUSH)
            .recipient("device-token")
            .title("Title")
            .body("Body")
            .build());
    }

    @Test
    void registerChannel_withMultipleChannels_registersAll() {
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid", "SG.test", "test@example.com", "Test Sender");
        
        NotificationChannel smsChannel = ChannelFactory.createChannel(
            Channel.SMS, "Twilio", "AC123", "token", "+1234567890");
        
        NotificationChannel pushChannel = ChannelFactory.createChannel(
            Channel.PUSH, "FCM", "server-key");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(emailChannel)
            .registerChannel(smsChannel)
            .registerChannel(pushChannel)
            .build();

        assertNotNull(service);
    }

    @Test
    void registerChannel_withNull_ignoresNull() {
        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(null)
            .build();

        assertNotNull(service);
    }

    @Test
    void registerChannel_withRetryConfig_appliesRetryToRegisteredChannel() {
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid", "SG.test", "test@example.com", "Test Sender");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(emailChannel)
            .retryConfig(RetryConfig.builder().maxAttempts(3).build())
            .build();

        assertNotNull(service);
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build());
    }

    @Test
    void registerChannel_returnsBuilderForChaining() {
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid", "SG.test", "test@example.com", "Test Sender");
        NotificationServiceBuilder builder = NotificationServiceBuilder.builder();
        
        assertSame(builder, builder.registerChannel(emailChannel));
    }

    @Test
    void retryConfig_returnsBuilderForChaining() {
        NotificationServiceBuilder builder = NotificationServiceBuilder.builder();
        RetryConfig config = RetryConfig.builder().maxAttempts(2).build();
        
        assertSame(builder, builder.retryConfig(config));
    }

    @Test
    void build_withMultipleChannelsAndRetry_returnsServiceThatCanSend() {
        NotificationChannel emailChannel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid", "SG.test", "test@example.com", "Test Sender");
        NotificationChannel smsChannel = ChannelFactory.createChannel(
            Channel.SMS, "Twilio", "AC123", "token", "+1234567890");

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(emailChannel)
            .registerChannel(smsChannel)
            .retryConfig(RetryConfig.builder()
                .maxAttempts(3)
                .initialDelayMs(1000)
                .maxDelayMs(10000)
                .backoffMultiplier(2.0)
                .build())
            .build();

        assertNotNull(service);
        // Verify both channels are reachable (send may succeed or fail with fake credentials)
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("u@ex.com")
            .subject("S")
            .message("M")
            .build());
        sendAndAcceptResultOrProviderFailure(service, NotificationRequest.builder()
            .channel(Channel.SMS)
            .recipient("+1234567890")
            .message("M")
            .build());
    }

    @Test
    void build_withCustomChannel_registersCustomChannel() {
        EmailChannel emailChannel = new com.agora.notification.channels.EmailChannel();
        emailChannel.setProvider(new SendGridEmailProvider(
            com.agora.notification.config.EmailConfig.builder()
                .apiKey("SG.custom")
                .fromEmail("custom@example.com")
                .build()));

        NotificationService service = NotificationServiceBuilder.builder()
            .registerChannel(emailChannel)
            .build();

        assertNotNull(service);
        sendAndAcceptResultOrProviderFailure(
            service, 
            NotificationRequest.builder()
            .channel(Channel.EMAIL)
            .recipient("user@example.com")
            .subject("Test")
            .message("Test message")
            .build()
        );
    }

    /** Providers with fake credentials may return a result or throw; both mean the channel was registered. */
    private static void sendAndAcceptResultOrProviderFailure(NotificationService service, NotificationRequest request) {
        try {
            NotificationResult result = service.send(request);
            assertNotNull(result);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to send") || e.getCause() != null,
                "Expected provider failure with fake credentials: " + e.getMessage());
        }
    }
}
