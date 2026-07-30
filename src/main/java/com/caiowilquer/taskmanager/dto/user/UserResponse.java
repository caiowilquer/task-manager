package com.caiowilquer.taskmanager.dto.user;

import com.caiowilquer.taskmanager.entity.enums.UserRole;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email, UserRole role) {
}
