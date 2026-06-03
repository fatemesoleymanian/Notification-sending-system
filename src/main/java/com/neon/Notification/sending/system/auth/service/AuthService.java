package com.neon.Notification.sending.system.auth.service;

import com.neon.Notification.sending.system.auth.dto.AuthResponse;
import com.neon.Notification.sending.system.auth.dto.LoginRequest;
import com.neon.Notification.sending.system.auth.dto.RegisterRequest;
import com.neon.Notification.sending.system.notification.dto.NotificationRequest;
import com.neon.Notification.sending.system.notification.service.NotificationService;
import com.neon.Notification.sending.system.notification.model.NotificationChannel;
import com.neon.Notification.sending.system.user.entity.AppUser;
import com.neon.Notification.sending.system.user.model.UserRole;
import com.neon.Notification.sending.system.user.repository.AppUserRepository;
import com.neon.Notification.sending.system.user.service.UserQueryService;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final UserQueryService userQueryService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            NotificationService notificationService,
            UserQueryService userQueryService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
        this.userQueryService = userQueryService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.ROLE_USER);

        AppUser saved = appUserRepository.save(user);
        notificationService.sendToUser(saved.getUsername(), new NotificationRequest(
                NotificationChannel.IN_APP,
                "Welcome",
                "Your account has been created successfully."
        ));
        String token = jwtService.generateToken(saved.getUsername(), Map.of("role", saved.getRole().name()));
        return new AuthResponse(token, "Bearer", saved.getId(), saved.getUsername(), saved.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        AppUser user = userQueryService.getByUsername(request.username());

        notificationService.sendToUser(user.getUsername(), new NotificationRequest(
                NotificationChannel.IN_APP,
                "Login successful",
                "You have successfully logged in."
        ));
        String token = jwtService.generateToken(user.getUsername(), Map.of("role", user.getRole().name()));
        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(), user.getRole().name());
    }
}
