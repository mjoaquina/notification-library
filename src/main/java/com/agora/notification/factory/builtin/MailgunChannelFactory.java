package com.agora.notification.factory.builtin;

import com.agora.notification.channels.EmailChannel;
import com.agora.notification.config.EmailConfig;
import com.agora.notification.core.NotificationChannel;
import com.agora.notification.factory.ChannelFactoryInterface;
import com.agora.notification.models.Channel;
import com.agora.notification.providers.email.MailgunEmailProvider;

/** 
 * Creates email channels backed by Mailgun. Config: apiKey, fromEmail, fromName. 
 */
public class MailgunChannelFactory implements ChannelFactoryInterface {

    @Override
    public NotificationChannel createChannel(String... config) {
        if (config == null || config.length < 3) {
            throw new IllegalArgumentException("Mailgun requires apiKey, fromEmail, fromName");
        }
        EmailConfig emailConfig = EmailConfig.builder()
                .apiKey(config[0])
                .fromEmail(config[1])
                .fromName(config[2])
                .apiUrl("https://api.mailgun.net")
                .timeoutMs(5000)
                .build();
        MailgunEmailProvider provider = new MailgunEmailProvider(emailConfig);
        return new EmailChannel(provider);
    }

    @Override
    public String getProviderName() {
        return "Mailgun";
    }

    @Override
    public String getChannelType() {
        return Channel.EMAIL.name();
    }
}
