package com.caiowilquer.taskmanager.integration;

import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.UserRole;
import com.caiowilquer.taskmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class CriticalTaskFlowIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate restTemplate;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void createAdmin() {
        if (!userRepository.existsByEmail("admin@integration.local")) {
            userRepository.saveAndFlush(User.create("Integration Admin", "admin@integration.local",
                    passwordEncoder.encode("Admin@123"), UserRole.ADMIN));
        }
    }

    @Test
    void shouldEnforceCriticalTaskWorkflowThroughRestApi() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String memberEmail = "member-" + suffix + "@integration.local";

        ResponseEntity<String> register = post("/api/v1/auth/register", null, Map.of(
                "name", "Integration Member", "email", memberEmail, "password", "Member@123"));
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String memberId = objectMapper.readTree(register.getBody()).get("id").asText();

        String adminToken = login("admin@integration.local", "Admin@123");
        ResponseEntity<String> createProject = post("/api/v1/projects", adminToken,
                Map.of("name", "Critical Flow " + suffix, "description", "Integration test project"));
        assertThat(createProject.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String projectId = objectMapper.readTree(createProject.getBody()).get("id").asText();

        ResponseEntity<String> addMember = post("/api/v1/projects/" + projectId + "/members", adminToken,
                Map.of("email", memberEmail));
        assertThat(addMember.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> createTask = post("/api/v1/projects/" + projectId + "/tasks", adminToken,
                Map.of("title", "Fix critical incident", "description", "Must be closed by project admin",
                        "priority", "CRITICAL", "assigneeId", memberId));
        assertThat(createTask.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String taskId = objectMapper.readTree(createTask.getBody()).get("id").asText();

        String memberToken = login(memberEmail, "Member@123");
        ResponseEntity<String> memberDone = patchStatus(projectId, taskId, memberToken, "DONE");
        assertThat(memberDone.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<String> inProgress = patchStatus(projectId, taskId, memberToken, "IN_PROGRESS");
        assertThat(inProgress.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> adminDone = patchStatus(projectId, taskId, adminToken, "DONE");
        assertThat(adminDone.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(adminDone.getBody()).get("status").asText()).isEqualTo("DONE");

        ResponseEntity<String> backToTodo = patchStatus(projectId, taskId, adminToken, "TODO");
        assertThat(backToTodo.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        ResponseEntity<String> summary = exchange("/api/v1/projects/" + projectId + "/tasks/summary",
                HttpMethod.GET, adminToken, null);
        JsonNode summaryJson = objectMapper.readTree(summary.getBody());
        assertThat(summaryJson.path("byStatus").path("DONE").asLong()).isEqualTo(1L);
    }

    private String login(String email, String password) throws Exception {
        ResponseEntity<String> response = post("/api/v1/auth/login", null,
                Map.of("email", email, "password", password));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody()).get("token").asText();
    }

    private ResponseEntity<String> patchStatus(String projectId, String taskId, String token, String status) {
        return exchange("/api/v1/projects/" + projectId + "/tasks/" + taskId + "/status",
                HttpMethod.PATCH, token, Map.of("status", status));
    }

    private ResponseEntity<String> post(String path, String token, Object body) {
        return exchange(path, HttpMethod.POST, token, body);
    }

    private ResponseEntity<String> exchange(String path, HttpMethod method, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return restTemplate.exchange("http://localhost:" + port + path, method,
                new HttpEntity<>(body, headers), String.class);
    }
}
