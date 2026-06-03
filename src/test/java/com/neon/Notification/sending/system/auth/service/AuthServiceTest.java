package com.neon.Notification.sending.system.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neon.Notification.sending.system.auth.dto.AuthResponse;
import com.neon.Notification.sending.system.auth.dto.LoginRequest;
import com.neon.Notification.sending.system.auth.dto.RegisterRequest;
import com.neon.Notification.sending.system.notification.model.NotificationChannel;
import com.neon.Notification.sending.system.notification.service.NotificationService;
import com.neon.Notification.sending.system.user.entity.AppUser;
import com.neon.Notification.sending.system.user.model.UserRole;
import com.neon.Notification.sending.system.user.repository.AppUserRepository;
import com.neon.Notification.sending.system.user.service.UserQueryService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserQueryService userQueryService;

    @Captor
    private ArgumentCaptor<com.neon.Notification.sending.system.notification.dto.NotificationRequest> notificationRequestCaptor;

    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("U2VjcmV0S2V5Rm9yTXlNaW5pRVJQU3lzdGVtMTIzIT8=", 86_400_000L);
        authService = new AuthService(
                appUserRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                notificationService,
                userQueryService
        );
    }

    @Test
    void registerShouldCreateUserAndIssueJwt() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "Password123!");
        AppUser saved = new AppUser();
        saved.setId(1L);
        saved.setUsername("alice");
        saved.setEmail("alice@example.com");
        saved.setPassword("encoded");
        saved.setRole(UserRole.ROLE_USER);

        when(appUserRepository.existsByUsername("alice")).thenReturn(false);
        when(appUserRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(saved);

        AuthResponse response = authService.register(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo("ROLE_USER");
        assertThat(response.token()).isNotBlank();

        verify(notificationService).sendToUser(eq("alice"), notificationRequestCaptor.capture());
        assertThat(notificationRequestCaptor.getValue().channel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(notificationRequestCaptor.getValue().title()).isEqualTo("Welcome");
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        when(appUserRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(new RegisterRequest("alice", "alice@example.com", "Password123!")));

        verify(appUserRepository, never()).save(any());
        verify(notificationService, never()).sendToUser(anyString(), any());
    }

    @Test
    void loginShouldAuthenticateAndIssueJwt() {
        LoginRequest request = new LoginRequest("alice", "Password123!");
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("encoded");
        user.setRole(UserRole.ROLE_USER);

        when(userQueryService.getByUsername("alice")).thenReturn(user);

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("alice", "Password123!"));
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo("ROLE_USER");
        assertThat(response.token()).isNotBlank();

        verify(notificationService).sendToUser(eq("alice"), notificationRequestCaptor.capture());
        assertThat(notificationRequestCaptor.getValue().channel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(notificationRequestCaptor.getValue().title()).isEqualTo("Login successful");
    }
}
