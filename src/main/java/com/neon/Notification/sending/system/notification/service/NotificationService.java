package com.neon.Notification.sending.system.notification.service;

import com.neon.Notification.sending.system.notification.dto.NotificationRequest;
import com.neon.Notification.sending.system.notification.dto.NotificationResponse;
import com.neon.Notification.sending.system.notification.entity.NotificationRecord;
import com.neon.Notification.sending.system.notification.model.NotificationChannel;
import com.neon.Notification.sending.system.notification.model.NotificationStatus;
import com.neon.Notification.sending.system.notification.repository.NotificationRepository;
import com.neon.Notification.sending.system.user.entity.AppUser;
import com.neon.Notification.sending.system.user.service.UserQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserQueryService userQueryService;

    public NotificationService(NotificationRepository notificationRepository, UserQueryService userQueryService) {
        this.notificationRepository = notificationRepository;
        this.userQueryService = userQueryService;
    }

    @Transactional
    @CacheEvict(cacheNames = "notificationHistory", allEntries = true)
    public NotificationResponse sendToUser(String username, NotificationRequest request) {
        AppUser user = userQueryService.getByUsername(username);

        NotificationRecord record = new NotificationRecord();
        record.setUser(user);
        record.setTitle(request.title());
        record.setMessage(request.message());
        record.setChannel(request.channel());
        record.setStatus(resolveStatus(request.channel()));
        if (record.getStatus() == NotificationStatus.SENT) {
            record.setSentAt(LocalDateTime.now());
        }

        NotificationRecord saved = notificationRepository.save(record);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "notificationHistory", key = "#username")
    public List<NotificationResponse> getHistory(String username) {
        AppUser user = userQueryService.getByUsername(username);

        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(String username, Long notificationId) {
        AppUser user = userQueryService.getByUsername(username);

        NotificationRecord record = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationStatus resolveStatus(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL, SMS, IN_APP -> NotificationStatus.SENT;
        };
    }

    private NotificationResponse toResponse(NotificationRecord record) {
        return new NotificationResponse(
                record.getId(),
                record.getUser().getId(),
                record.getUser().getUsername(),
                record.getTitle(),
                record.getMessage(),
                record.getChannel(),
                record.getStatus(),
                record.getCreatedAt(),
                record.getSentAt()
        );
    }
}
