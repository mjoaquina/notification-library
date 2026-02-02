package com.agora.notification.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Payload for a single notification. Channel and recipient are required; subject/title/body
 * depend on the channel (e.g. email uses subject+message, push uses title+body).
 */
@Getter
@Builder
public class NotificationRequest {
    private final Channel channel;
    private final String recipient;
    private final String subject;
    private final String message;
    private final String title;
    private final String body;
}
