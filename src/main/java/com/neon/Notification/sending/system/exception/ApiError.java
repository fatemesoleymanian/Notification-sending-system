package com.neon.Notification.sending.system.exception;

public record ApiError(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
