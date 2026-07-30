package com.caiowilquer.taskmanager.dto.task;

import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 160, message = "O título deve ter no máximo 160 caracteres.")
        String title,
        @Size(max = 4000, message = "A descrição deve ter no máximo 4.000 caracteres.")
        String description,
        @NotNull(message = "A definição de prioridade é necessária.")
        TaskPriority priority,
        @FutureOrPresent(message = "O prazo não pode ser uma data passada.")
        LocalDate deadline,
        @NotNull(message = "O responsável é obrigatório.")
        UUID assigneeId
) {
}
