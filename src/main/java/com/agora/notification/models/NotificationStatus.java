package com.agora.notification.models;

/** 
 * Outcome of a send attempt: PENDING, SENT, FAILED, or RETRYING. 
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    RETRYING
}
