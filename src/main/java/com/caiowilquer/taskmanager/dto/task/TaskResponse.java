package com.caiowilquer.taskmanager.dto.task;

import com.caiowilquer.taskmanager.dto.user.UserResponse;
import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate deadline,
        UserResponse assignee,
        UserResponse createdBy,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
