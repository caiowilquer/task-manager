package com.caiowilquer.taskmanager.config;

import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.UserRole;
import com.caiowilquer.taskmanager.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true")
public class DataInitializer implements ApplicationRunner {

    private final BootstrapProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(BootstrapProperties properties,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfMissing(properties.adminName(), properties.adminEmail(),
                properties.adminPassword(), UserRole.ADMIN);
        createIfMissing(properties.memberName(), properties.memberEmail(),
                properties.memberPassword(), UserRole.MEMBER);
    }

    private void createIfMissing(String name, String email, String password, UserRole role) {
        String normalizedEmail = User.normalizeEmail(email);
        if (!userRepository.existsByEmail(normalizedEmail)) {
            userRepository.save(User.create(name, normalizedEmail, passwordEncoder.encode(password), role));
        }
    }
}
