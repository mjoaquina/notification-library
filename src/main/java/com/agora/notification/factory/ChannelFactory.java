package com.agora.notification.factory;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.models.Channel;

/**
 * Entry point for creating notification channels. Delegates to the registry;
 * built-in providers (SendGrid, Twilio, FCM, etc.) are registered on load.
 * Use {@link #getRegistry()} to register custom providers.
 */
public final class ChannelFactory {

    private static final ChannelFactoryRegistryInterface registry = ChannelFactoryRegistry.getInstance();

    static {
        BuiltInChannelFactoryRegistrar.registerAll(registry);
    }

    private ChannelFactory() {
        // Utility class
    }

    /**
     * Creates a channel for the given type and provider. Config order depends on the provider
     * (e.g. SendGrid: apiKey, fromEmail, fromName).
     *
     * @return The channel, or null if no factory is registered for that type and provider
     */
    public static NotificationChannel createChannel(Channel channelType, String providerName, String... config) {
        return registry.createChannel(channelType.name(), providerName, config);
    }

    /** Returns the registry; use it to {@link ChannelFactoryRegistryInterface#register(ChannelFactoryInterface) register} custom providers. */
    public static ChannelFactoryRegistryInterface getRegistry() {
        return registry;
    }
}
