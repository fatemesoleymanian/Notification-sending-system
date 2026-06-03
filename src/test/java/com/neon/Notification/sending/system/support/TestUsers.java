package com.neon.Notification.sending.system.support;

import com.neon.Notification.sending.system.auth.dto.LoginRequest;
import com.neon.Notification.sending.system.auth.dto.RegisterRequest;
import com.neon.Notification.sending.system.notification.dto.NotificationRequest;
import com.neon.Notification.sending.system.notification.model.NotificationChannel;

public final class TestUsers {

    public static final String USERNAME = "alice";
    public static final String PASSWORD = "Password123!";
    public static final String EMAIL = "alice@example.com";

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "Admin123!";
    public static final String ADMIN_EMAIL = "admin@example.com";

    private TestUsers() {
    }

    public static RegisterRequest registerRequest() {
        return new RegisterRequest(USERNAME, EMAIL, PASSWORD);
    }

    public static LoginRequest loginRequest() {
        return new LoginRequest(USERNAME, PASSWORD);
    }

    public static LoginRequest adminLoginRequest() {
        return new LoginRequest(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public static NotificationRequest notificationRequest(NotificationChannel channel) {
        return new NotificationRequest(channel, "Test title", "Test message");
    }
}
