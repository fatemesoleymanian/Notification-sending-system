package com.neon.Notification.sending.system.notification.dto;

import com.neon.Notification.sending.system.notification.model.NotificationChannel;
import com.neon.Notification.sending.system.notification.model.NotificationStatus;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String username,
        String title,
        String message,
        NotificationChannel channel,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime sentAt
) {
}
