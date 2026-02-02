package com.agora.notification.factory.builtin;

import com.agora.notification.channels.PushChannel;
import com.agora.notification.config.PushConfig;
import com.agora.notification.core.NotificationChannel;
import com.agora.notification.factory.ChannelFactoryInterface;
import com.agora.notification.models.Channel;
import com.agora.notification.providers.push.FcmPushProvider;

/** 
 * Creates push channels backed by Firebase Cloud Messaging. Config: serverKey. 
 */
public class FcmChannelFactory implements ChannelFactoryInterface {

    @Override
    public NotificationChannel createChannel(String... config) {
        if (config == null || config.length < 1) {
            throw new IllegalArgumentException("FCM requires serverKey");
        }
        PushConfig pushConfig = PushConfig.builder()
                .serverKey(config[0])
                .apiUrl("https://fcm.googleapis.com")
                .timeoutMs(5000)
                .build();
        FcmPushProvider provider = new FcmPushProvider(pushConfig);
        return new PushChannel(provider);
    }

    @Override
    public String getProviderName() {
        return "FCM";
    }

    @Override
    public String getChannelType() {
        return Channel.PUSH.name();
    }
}
