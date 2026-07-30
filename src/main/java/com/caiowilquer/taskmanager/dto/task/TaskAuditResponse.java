package com.caiowilquer.taskmanager.dto.task;

import com.caiowilquer.taskmanager.dto.user.UserResponse;
import com.caiowilquer.taskmanager.entity.enums.AuditAction;

import java.time.Instant;
import java.util.UUID;

public record TaskAuditResponse(
        UUID id,
        AuditAction action,
        String fieldName,
        String previousValue,
        String newValue,
        UserResponse changedBy,
        Instant changedAt
) {
}
