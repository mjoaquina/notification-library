package com.agora.notification.factory;

import com.agora.notification.core.NotificationChannel;

/**
 * Contract for creating a notification channel for a specific provider.
 * Implementations are registered in the registry and selected by channel type + provider name.
 */
public interface ChannelFactoryInterface {

    /** Builds a channel from the given config (order and count are provider-specific). */
    NotificationChannel createChannel(String... config);

    /** Provider identifier used as part of the registry key (e.g. "SendGrid", "Twilio"). */
    String getProviderName();

    /** Channel type used as part of the registry key (e.g. "EMAIL", "SMS", "PUSH"). */
    String getChannelType();
}
