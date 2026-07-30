package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.dto.auth.AuthResponse;
import com.caiowilquer.taskmanager.dto.auth.LoginRequest;
import com.caiowilquer.taskmanager.dto.auth.RegisterRequest;
import com.caiowilquer.taskmanager.dto.user.UserResponse;
import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.UserRole;
import com.caiowilquer.taskmanager.exception.ConflictException;
import com.caiowilquer.taskmanager.mapper.UserMapper;
import com.caiowilquer.taskmanager.repository.UserRepository;
import com.caiowilquer.taskmanager.security.JwtService;
import com.caiowilquer.taskmanager.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final CurrentUserService currentUserService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserMapper userMapper,
                       CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = User.normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Já existe um usuário cadastrado com este email.");
        }
        User user = User.create(request.name(), normalizedEmail,
                passwordEncoder.encode(request.password()), UserRole.MEMBER);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(User.normalizeEmail(request.email()), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId()).orElseThrow();
        return new AuthResponse(jwtService.generateToken(principal), "Bearer",
                jwtService.expirationSeconds(), userMapper.toResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return userMapper.toResponse(currentUserService.entity());
    }
}
