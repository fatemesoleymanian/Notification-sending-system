package com.neon.Notification.sending.system.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neon.Notification.sending.system.user.entity.AppUser;
import com.neon.Notification.sending.system.user.model.UserRole;
import com.neon.Notification.sending.system.user.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserQueryServiceTest {

    @Autowired
    private UserQueryService userQueryService;

    @MockBean
    private AppUserRepository appUserRepository;

    @Test
    void userLookupShouldBeCachedByUsername() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("encoded");
        user.setRole(UserRole.ROLE_USER);

        when(appUserRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        AppUser first = userQueryService.getByUsername("alice");
        AppUser second = userQueryService.getByUsername("alice");

        assertThat(first.getUsername()).isEqualTo("alice");
        assertThat(second.getUsername()).isEqualTo("alice");
        verify(appUserRepository, times(1)).findByUsername("alice");
    }
}
