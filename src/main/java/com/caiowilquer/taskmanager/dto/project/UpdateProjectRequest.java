package com.caiowilquer.taskmanager.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String name,
        @Size(max = 1000, message = "A descrição deve ter no máximo 1.000 caracteres.")
        String description
) {
}
