package com.agora.notification.factory;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.models.Channel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelFactoryTest {

    @Test
    void createChannel_withSendGrid_createsConfiguredEmailChannel() {
        NotificationChannel channel = ChannelFactory.createChannel(
            Channel.EMAIL, "SendGrid", "SG.key", "from@example.com", "Sender");

        assertNotNull(channel);
        assertNotNull(channel.getProvider());
        assertEquals(Channel.EMAIL, channel.getChannelType());
        assertTrue(channel.getProvider().getName().contains("SendGrid"));
    }

    @Test
    void createChannel_withMailgun_createsConfiguredEmailChannel() {
        NotificationChannel channel = ChannelFactory.createChannel(
            Channel.EMAIL, "Mailgun", "key-xxx", "from@example.com", "Sender");

        assertNotNull(channel);
        assertNotNull(channel.getProvider());
        assertEquals(Channel.EMAIL, channel.getChannelType());
        assertTrue(channel.getProvider().getName().contains("Mailgun"));
    }

    @Test
    void createChannel_withTwilio_createsConfiguredSmsChannel() {
        NotificationChannel channel = ChannelFactory.createChannel(
            Channel.SMS, "Twilio", "AC123", "auth-token", "+1234567890");

        assertNotNull(channel);
        assertNotNull(channel.getProvider());
        assertEquals(Channel.SMS, channel.getChannelType());
        assertTrue(channel.getProvider().getName().contains("Twilio"));
    }

    @Test
    void createChannel_withAwsSns_createsConfiguredSmsChannel() {
        NotificationChannel channel = ChannelFactory.createChannel(
            Channel.SMS, "AwsSns", "apiKey", "apiSecret", "us-east-1");

        assertNotNull(channel);
        assertNotNull(channel.getProvider());
        assertEquals(Channel.SMS, channel.getChannelType());
        assertTrue(channel.getProvider().getName().contains("AWS SNS"));
    }

    @Test
    void createChannel_withFcm_createsConfiguredPushChannel() {
        NotificationChannel channel = ChannelFactory.createChannel(
            Channel.PUSH, "FCM", "server-key");

        assertNotNull(channel);
        assertNotNull(channel.getProvider());
        assertEquals(Channel.PUSH, channel.getChannelType());
        assertTrue(channel.getProvider().getName().contains("FCM"));
    }

    @Test
    void createChannel_withOneSignal_createsConfiguredPushChannel() {
        NotificationChannel channel = ChannelFactory.createChannel(
            Channel.PUSH, "OneSignal", "api-key", "app-id-123");

        assertNotNull(channel);
        assertNotNull(channel.getProvider());
        assertEquals(Channel.PUSH, channel.getChannelType());
        assertTrue(channel.getProvider().getName().contains("OneSignal"));
    }
}
