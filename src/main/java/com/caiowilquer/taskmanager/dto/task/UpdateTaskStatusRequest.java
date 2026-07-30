package com.caiowilquer.taskmanager.dto.task;

import com.caiowilquer.taskmanager.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(@NotNull(message = "O status é obrigatório.") TaskStatus status) {
}
