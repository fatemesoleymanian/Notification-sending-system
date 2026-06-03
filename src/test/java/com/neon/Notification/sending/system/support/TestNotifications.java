package com.neon.Notification.sending.system.support;

import com.neon.Notification.sending.system.notification.dto.NotificationRequest;
import com.neon.Notification.sending.system.notification.model.NotificationChannel;

public final class TestNotifications {

    private TestNotifications() {
    }

    public static NotificationRequest emailRequest() {
        return new NotificationRequest(NotificationChannel.EMAIL, "Email title", "Email message");
    }

    public static NotificationRequest smsRequest() {
        return new NotificationRequest(NotificationChannel.SMS, "SMS title", "SMS message");
    }

    public static NotificationRequest inAppRequest() {
        return new NotificationRequest(NotificationChannel.IN_APP, "In-app title", "In-app message");
    }
}
