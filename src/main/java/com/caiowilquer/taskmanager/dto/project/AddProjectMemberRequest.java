package com.caiowilquer.taskmanager.dto.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddProjectMemberRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        String email
) {
}
