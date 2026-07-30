package com.caiowilquer.taskmanager.dto.project;

import com.caiowilquer.taskmanager.dto.user.UserResponse;

import java.time.Instant;

public record ProjectMemberResponse(UserResponse user, Instant joinedAt, boolean owner) {
}
