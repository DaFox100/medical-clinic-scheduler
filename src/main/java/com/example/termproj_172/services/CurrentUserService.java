package com.example.termproj_172.services;

import com.example.termproj_172.domainModels.AppUser;
import com.example.termproj_172.domainModels.AppUserRole;
import com.example.termproj_172.repositories.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getRequiredUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }

        return appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user record not found."));
    }

    public boolean isAdmin(AppUser user) {
        return user.getRole() == AppUserRole.ADMIN;
    }

    public boolean isProvider(AppUser user) {
        return user.getRole() == AppUserRole.PROVIDER;
    }

    public boolean isPatient(AppUser user) {
        return user.getRole() == AppUserRole.PATIENT;
    }
}
