package com.caiowilquer.taskmanager.dto.auth;

import com.caiowilquer.taskmanager.dto.user.UserResponse;

public record AuthResponse(String token, String tokenType, long expiresIn, UserResponse user) {
}
