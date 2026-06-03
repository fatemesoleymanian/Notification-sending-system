package com.neon.Notification.sending.system.notification.repository;

import com.neon.Notification.sending.system.notification.entity.NotificationRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationRecord, Long> {

    List<NotificationRecord> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<NotificationRecord> findByIdAndUserId(Long id, Long userId);
}
