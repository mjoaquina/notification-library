package com.agora.notification.factory;

import com.agora.notification.factory.builtin.*;

/**
 * Registers the six built-in providers (SendGrid, Mailgun, Twilio, AwsSns, FCM, OneSignal).
 * Invoked automatically when {@link ChannelFactory} is first used. To add another built-in,
 * implement {@link ChannelFactoryInterface} and register it here.
 */
public final class BuiltInChannelFactoryRegistrar {

    private BuiltInChannelFactoryRegistrar() {}

    /** Registers all built-in factories in the given registry. Null registry is ignored. */
    public static void registerAll(ChannelFactoryRegistryInterface registry) {
        if (registry == null) {
            return;
        }
        registry.register(new SendGridChannelFactory());
        registry.register(new MailgunChannelFactory());
        registry.register(new TwilioChannelFactory());
        registry.register(new AwsSnsChannelFactory());
        registry.register(new FcmChannelFactory());
        registry.register(new OneSignalChannelFactory());
    }
}
