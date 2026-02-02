package com.agora.notification.factory.builtin;

import com.agora.notification.channels.SmsChannel;
import com.agora.notification.config.SmsConfig;
import com.agora.notification.core.NotificationChannel;
import com.agora.notification.factory.ChannelFactoryInterface;
import com.agora.notification.models.Channel;
import com.agora.notification.providers.sms.AwsSnsProvider;

/** 
 * Creates SMS channels backed by AWS SNS. Config: apiKey, apiSecret, region. 
 */
public class AwsSnsChannelFactory implements ChannelFactoryInterface {
    
    @Override
    public NotificationChannel createChannel(String... config) {
        if (config == null || config.length < 3) {
            throw new IllegalArgumentException("AWS SNS requires apiKey, apiSecret, region");
        }
        String region = config[2];
        SmsConfig smsConfig = SmsConfig.builder()
                .apiKey(config[0])
                .apiSecret(config[1])
                .region(region)
                .apiUrl("https://sns." + region + ".amazonaws.com")
                .timeoutMs(5000)
                .build();
        AwsSnsProvider provider = new AwsSnsProvider(smsConfig);
        return new SmsChannel(provider);
    }

    @Override
    public String getProviderName() {
        return "AwsSns";
    }

    @Override
    public String getChannelType() {
        return Channel.SMS.name();
    }
}
