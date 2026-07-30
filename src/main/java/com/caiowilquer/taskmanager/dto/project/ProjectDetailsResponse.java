package com.caiowilquer.taskmanager.dto.project;

import com.caiowilquer.taskmanager.dto.user.UserResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectDetailsResponse(
        UUID id,
        String name,
        String description,
        UserResponse owner,
        List<ProjectMemberResponse> members,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
