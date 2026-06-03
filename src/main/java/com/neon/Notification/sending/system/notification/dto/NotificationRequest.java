package com.neon.Notification.sending.system.notification.dto;

import com.neon.Notification.sending.system.notification.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotNull NotificationChannel channel,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 1000) String message
) {
}
