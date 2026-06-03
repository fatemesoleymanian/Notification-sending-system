package com.neon.Notification.sending.system.notification.controller;

import com.neon.Notification.sending.system.notification.dto.NotificationRequest;
import com.neon.Notification.sending.system.notification.dto.NotificationResponse;
import com.neon.Notification.sending.system.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<NotificationResponse> send(
            Principal principal,
            @Valid @RequestBody NotificationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendToUser(principal.getName(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<NotificationResponse>> history(Principal principal) {
        return ResponseEntity.ok(notificationService.getHistory(principal.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<NotificationResponse> getById(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(principal.getName(), id));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/admin/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> historyForUser(@PathVariable String username) {
        return ResponseEntity.ok(notificationService.getHistory(username));
    }
}
