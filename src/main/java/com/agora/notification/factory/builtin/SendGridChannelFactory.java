package com.agora.notification.factory.builtin;

import com.agora.notification.channels.EmailChannel;
import com.agora.notification.config.EmailConfig;
import com.agora.notification.core.NotificationChannel;
import com.agora.notification.factory.ChannelFactoryInterface;
import com.agora.notification.models.Channel;
import com.agora.notification.providers.email.SendGridEmailProvider;

/** 
 * Creates email channels backed by SendGrid. Config: apiKey, fromEmail, fromName. 
 */
public class SendGridChannelFactory implements ChannelFactoryInterface {

    @Override
    public NotificationChannel createChannel(String... config) {
        if (config == null || config.length < 3) {
            throw new IllegalArgumentException("SendGrid requires apiKey, fromEmail, fromName");
        }
        EmailConfig emailConfig = EmailConfig.builder()
                .apiKey(config[0])
                .fromEmail(config[1])
                .fromName(config[2])
                .apiUrl("https://api.sendgrid.com")
                .timeoutMs(5000)
                .build();
        SendGridEmailProvider provider = new SendGridEmailProvider(emailConfig);
        return new EmailChannel(provider);
    }

    @Override
    public String getProviderName() {
        return "SendGrid";
    }

    @Override
    public String getChannelType() {
        return Channel.EMAIL.name();
    }
}
