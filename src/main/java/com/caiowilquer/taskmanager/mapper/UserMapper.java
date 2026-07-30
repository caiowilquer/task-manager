package com.caiowilquer.taskmanager.mapper;

import com.caiowilquer.taskmanager.dto.user.UserResponse;
import com.caiowilquer.taskmanager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
