package com.agora.notification.factory.builtin;

import com.agora.notification.channels.SmsChannel;
import com.agora.notification.config.SmsConfig;
import com.agora.notification.core.NotificationChannel;
import com.agora.notification.factory.ChannelFactoryInterface;
import com.agora.notification.models.Channel;
import com.agora.notification.providers.sms.TwilioSmsProvider;

/** 
 * Creates SMS channels backed by Twilio. Config: accountSid, authToken, fromNumber. 
 */
public class TwilioChannelFactory implements ChannelFactoryInterface {

    @Override
    public NotificationChannel createChannel(String... config) {
        if (config == null || config.length < 3) {
            throw new IllegalArgumentException("Twilio requires accountSid, authToken, fromNumber");
        }
        SmsConfig smsConfig = SmsConfig.builder()
                .accountSid(config[0])
                .authToken(config[1])
                .fromNumber(config[2])
                .apiUrl("https://api.twilio.com")
                .timeoutMs(5000)
                .build();
        TwilioSmsProvider provider = new TwilioSmsProvider(smsConfig);
        return new SmsChannel(provider);
    }

    @Override
    public String getProviderName() {
        return "Twilio";
    }

    @Override
    public String getChannelType() {
        return Channel.SMS.name();
    }
}
