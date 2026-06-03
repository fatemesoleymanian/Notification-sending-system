package com.neon.Notification.sending.system.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neon.Notification.sending.system.notification.dto.NotificationRequest;
import com.neon.Notification.sending.system.notification.dto.NotificationResponse;
import com.neon.Notification.sending.system.notification.entity.NotificationRecord;
import com.neon.Notification.sending.system.notification.model.NotificationChannel;
import com.neon.Notification.sending.system.notification.model.NotificationStatus;
import com.neon.Notification.sending.system.notification.repository.NotificationRepository;
import com.neon.Notification.sending.system.support.TestNotifications;
import com.neon.Notification.sending.system.user.entity.AppUser;
import com.neon.Notification.sending.system.user.model.UserRole;
import com.neon.Notification.sending.system.user.repository.AppUserRepository;
import com.neon.Notification.sending.system.user.service.UserQueryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private UserQueryService userQueryService;

    @MockBean
    private AppUserRepository appUserRepository;

    @Test
    void sendToUserShouldPersistSentNotificationForEachChannel() {
        AppUser user = user("alice");
        when(userQueryService.getByUsername("alice")).thenReturn(user);
        when(notificationRepository.save(any(NotificationRecord.class))).thenAnswer(invocation -> {
            NotificationRecord record = invocation.getArgument(0);
            record.setId(10L);
            if (record.getCreatedAt() == null) {
                record.setCreatedAt(LocalDateTime.now());
            }
            return record;
        });

        NotificationResponse email = notificationService.sendToUser("alice", TestNotifications.emailRequest());
        NotificationResponse sms = notificationService.sendToUser("alice", TestNotifications.smsRequest());
        NotificationResponse inApp = notificationService.sendToUser("alice", TestNotifications.inAppRequest());

        assertThat(email.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(email.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(sms.channel()).isEqualTo(NotificationChannel.SMS);
        assertThat(inApp.channel()).isEqualTo(NotificationChannel.IN_APP);
        verify(notificationRepository, times(3)).save(any(NotificationRecord.class));
    }

    @Test
    void notificationHistoryShouldBeCachedAndInvalidatedAfterSend() {
        AppUser user = user("alice");
        NotificationRecord record = notificationRecord(user, 1L, NotificationChannel.IN_APP);

        when(userQueryService.getByUsername("alice")).thenReturn(user);
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of(record));
        when(notificationRepository.save(any(NotificationRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<NotificationResponse> first = notificationService.getHistory("alice");
        List<NotificationResponse> second = notificationService.getHistory("alice");

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        verify(notificationRepository, times(1)).findAllByUserIdOrderByCreatedAtDesc(user.getId());

        notificationService.sendToUser("alice", TestNotifications.inAppRequest());

        List<NotificationResponse> third = notificationService.getHistory("alice");

        assertThat(third).hasSize(1);
        verify(notificationRepository, times(2)).findAllByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Test
    void getByIdShouldReturnOnlyOwnNotification() {
        AppUser user = user("alice");
        NotificationRecord record = notificationRecord(user, 1L, NotificationChannel.EMAIL);

        when(userQueryService.getByUsername("alice")).thenReturn(user);
        when(notificationRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(record));

        NotificationResponse response = notificationService.getById("alice", 1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.channel()).isEqualTo(NotificationChannel.EMAIL);
        verify(notificationRepository, times(1)).findByIdAndUserId(1L, user.getId());
    }

    private AppUser user(String username) {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded");
        user.setRole(UserRole.ROLE_USER);
        return user;
    }

    private NotificationRecord notificationRecord(AppUser user, Long id, NotificationChannel channel) {
        NotificationRecord record = new NotificationRecord();
        record.setId(id);
        record.setUser(user);
        record.setTitle("Title");
        record.setMessage("Message");
        record.setChannel(channel);
        record.setStatus(NotificationStatus.SENT);
        record.setCreatedAt(LocalDateTime.now());
        record.setSentAt(LocalDateTime.now());
        return record;
    }
}
