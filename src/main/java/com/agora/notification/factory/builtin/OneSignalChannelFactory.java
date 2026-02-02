package com.agora.notification.factory.builtin;

import com.agora.notification.channels.PushChannel;
import com.agora.notification.config.PushConfig;
import com.agora.notification.core.NotificationChannel;
import com.agora.notification.factory.ChannelFactoryInterface;
import com.agora.notification.models.Channel;
import com.agora.notification.providers.push.OneSignalProvider;

/** 
 * Creates push channels backed by OneSignal. Config: apiKey, appId. 
 */
public class OneSignalChannelFactory implements ChannelFactoryInterface {

    @Override
    public NotificationChannel createChannel(String... config) {
        if (config == null || config.length < 2) {
            throw new IllegalArgumentException("OneSignal requires apiKey, appId");
        }
        PushConfig pushConfig = PushConfig.builder()
                .apiKey(config[0])
                .appId(config[1])
                .apiUrl("https://onesignal.com/api/v1")
                .timeoutMs(5000)
                .build();
        OneSignalProvider provider = new OneSignalProvider(pushConfig);
        return new PushChannel(provider);
    }

    @Override
    public String getProviderName() {
        return "OneSignal";
    }

    @Override
    public String getChannelType() {
        return Channel.PUSH.name();
    }
}
