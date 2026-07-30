package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.exception.ResourceNotFoundException;
import com.caiowilquer.taskmanager.repository.UserRepository;
import com.caiowilquer.taskmanager.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("Usuário autenticado não foi encontrado");
        }
        return principal;
    }

    public UUID id() {
        return principal().getId();
    }

    @Transactional(readOnly = true)
    public User entity() {
        return userRepository.findById(id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não foi encontrado."));
    }
}
