package com.caiowilquer.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap")
public record BootstrapProperties(
        boolean enabled,
        String adminName,
        String adminEmail,
        String adminPassword,
        String memberName,
        String memberEmail,
        String memberPassword
) {
}
