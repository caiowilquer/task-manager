package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.dto.auth.RegisterRequest;
import com.caiowilquer.taskmanager.dto.user.UserResponse;
import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.UserRole;
import com.caiowilquer.taskmanager.exception.ConflictException;
import com.caiowilquer.taskmanager.mapper.UserMapper;
import com.caiowilquer.taskmanager.repository.UserRepository;
import com.caiowilquer.taskmanager.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock UserMapper userMapper;
    @Mock CurrentUserService currentUserService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, passwordEncoder, authenticationManager,
                jwtService, userMapper, currentUserService);
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        RegisterRequest request = new RegisterRequest("User", "USER@test.local", "Password@123");
        when(userRepository.existsByEmail(User.normalizeEmail(request.email()))).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRegisterNewUsersAsMembersWithNormalizedEmailAndEncodedPassword() {
        RegisterRequest request = new RegisterRequest("  New User  ", "USER@Test.Local", "Password@123");
        UserResponse expected = new UserResponse(UUID.randomUUID(), "New User", "user@test.local", UserRole.MEMBER);
        when(userRepository.existsByEmail("user@test.local")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(expected);

        UserResponse response = service.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("user@test.local");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.MEMBER);
        assertThat(response).isEqualTo(expected);
    }
}
