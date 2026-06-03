package com.neon.Notification.sending.system.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String role
) {
}
