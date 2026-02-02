package com.agora.notification.factory;

import com.agora.notification.core.NotificationChannel;

/**
 * Registry of channel factories. Lookup by channel type + provider name; 
 * keys are case-insensitive for channel type (e.g. "EMAIL:SendGrid").
 */
public interface ChannelFactoryRegistryInterface {

    void register(ChannelFactoryInterface factory);

    /** @return The channel, or null if no factory is registered for that key */
    NotificationChannel createChannel(String channelType, String providerName, String... config);

    boolean isRegistered(String channelType, String providerName);

    void unregister(String channelType, String providerName);

    void clear();
}
