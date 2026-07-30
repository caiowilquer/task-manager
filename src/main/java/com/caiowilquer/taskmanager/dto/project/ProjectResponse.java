package com.caiowilquer.taskmanager.dto.project;

import com.caiowilquer.taskmanager.dto.user.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        UserResponse owner,
        long memberCount,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
