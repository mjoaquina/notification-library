package com.agora.notification.service;

import com.agora.notification.core.NotificationChannel;
import com.agora.notification.models.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.Map;

/** 
 * Holds one NotificationChannel per Channel type; used by the service to resolve channel by request. 
 */
@Slf4j
@Getter
public class NotificationChannelRegistry {

    private final Map<Channel, NotificationChannel> channels = new EnumMap<>(Channel.class);

    public void register(NotificationChannel channel) {
        if (channel != null) {
            channels.put(channel.getChannelType(), channel);
            log.debug("Registered channel: {}", channel.getChannelType());
        }
    }

    /** @return The channel for the given type, or null if none registered */
    public NotificationChannel getChannel(Channel channelType) {
        return channels.get(channelType);
    }

    public void unregister(Channel channelType) {
        channels.remove(channelType);
        log.debug("Unregistered channel: {}", channelType);
    }

    public void clear() {
        channels.clear();
        log.debug("Cleared all channels");
    }
}
