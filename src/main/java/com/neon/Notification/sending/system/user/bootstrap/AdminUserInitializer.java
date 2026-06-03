package com.neon.Notification.sending.system.user.bootstrap;

import com.neon.Notification.sending.system.user.entity.AppUser;
import com.neon.Notification.sending.system.user.model.UserRole;
import com.neon.Notification.sending.system.user.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String username;
    private final String email;
    private final String password;

    public AdminUserInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.enabled:true}") boolean enabled,
            @Value("${app.bootstrap.admin.username:admin}") String username,
            @Value("${app.bootstrap.admin.email:admin@example.com}") String email,
            @Value("${app.bootstrap.admin.password:Admin123!}") String password
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        if (appUserRepository.existsByUsername(username) || appUserRepository.existsByEmail(email)) {
            return;
        }

        AppUser admin = new AppUser();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(UserRole.ROLE_ADMIN);
        admin.setEnabled(true);

        appUserRepository.save(admin);
    }
}
