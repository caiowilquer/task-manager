package com.caiowilquer.taskmanager.controller;

import com.caiowilquer.taskmanager.dto.auth.AuthResponse;
import com.caiowilquer.taskmanager.dto.auth.LoginRequest;
import com.caiowilquer.taskmanager.dto.auth.RegisterRequest;
import com.caiowilquer.taskmanager.dto.user.UserResponse;
import com.caiowilquer.taskmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "\n" + "Cadastrar um usuário do tipo MEMBRO")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "\n" + "Autentique-se com e-mail e senha")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "\n" + "Obter usuário autenticado\n")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}
