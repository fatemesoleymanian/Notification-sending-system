package com.neon.Notification.sending.system.user.service;

import com.neon.Notification.sending.system.user.entity.AppUser;
import com.neon.Notification.sending.system.user.repository.AppUserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserQueryService {

    private final AppUserRepository appUserRepository;

    public UserQueryService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "usersByUsername", key = "#username")
    public AppUser getByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "usersById", key = "#id")
    public AppUser getById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
