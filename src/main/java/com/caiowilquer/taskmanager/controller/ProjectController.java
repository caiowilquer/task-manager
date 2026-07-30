package com.caiowilquer.taskmanager.controller;

import com.caiowilquer.taskmanager.dto.common.PageResponse;
import com.caiowilquer.taskmanager.dto.project.AddProjectMemberRequest;
import com.caiowilquer.taskmanager.dto.project.CreateProjectRequest;
import com.caiowilquer.taskmanager.dto.project.ProjectDetailsResponse;
import com.caiowilquer.taskmanager.dto.project.ProjectMemberResponse;
import com.caiowilquer.taskmanager.dto.project.ProjectResponse;
import com.caiowilquer.taskmanager.dto.project.UpdateProjectRequest;
import com.caiowilquer.taskmanager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@Validated
@Tag(name = "Projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar um projeto")
    public ResponseEntity<ProjectDetailsResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectDetailsResponse response = projectService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar projetos acessíveis ao usuário autenticado")
    public PageResponse<ProjectResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return projectService.list(page, size, sortBy, direction);
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Obter detalhes do projeto")
    public ProjectDetailsResponse get(@PathVariable UUID projectId) {
        return projectService.get(projectId);
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Atualizar um projeto (apenas o proprietário)")
    public ProjectDetailsResponse update(@PathVariable UUID projectId,
                                         @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um projeto (apenas o proprietário)")
    public void delete(@PathVariable UUID projectId) {
        projectService.delete(projectId);
    }

    @PostMapping("/{projectId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar um membro ao projeto (apenas proprietário)")
    public ProjectMemberResponse addMember(@PathVariable UUID projectId,
                                           @Valid @RequestBody AddProjectMemberRequest request) {
        return projectService.addMember(projectId, request);
    }

    @GetMapping("/{projectId}/members")
    @Operation(summary = "Listar membros do projeto")
    public List<ProjectMemberResponse> listMembers(@PathVariable UUID projectId) {
        return projectService.listMembers(projectId);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover um membro do projeto (apenas o proprietário)")
    public void removeMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        projectService.removeMember(projectId, userId);
    }
}
