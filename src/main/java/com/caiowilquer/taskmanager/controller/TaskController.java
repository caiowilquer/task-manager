package com.caiowilquer.taskmanager.controller;

import com.caiowilquer.taskmanager.dto.common.PageResponse;
import com.caiowilquer.taskmanager.dto.task.CreateTaskRequest;
import com.caiowilquer.taskmanager.dto.task.TaskAuditResponse;
import com.caiowilquer.taskmanager.dto.task.TaskFilterParams;
import com.caiowilquer.taskmanager.dto.task.TaskResponse;
import com.caiowilquer.taskmanager.dto.task.TaskSummaryResponse;
import com.caiowilquer.taskmanager.dto.task.UpdateTaskRequest;
import com.caiowilquer.taskmanager.dto.task.UpdateTaskStatusRequest;
import com.caiowilquer.taskmanager.service.TaskAuditService;
import com.caiowilquer.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@Validated
@Tag(name = "Tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskAuditService auditService;

    public TaskController(TaskService taskService, TaskAuditService auditService) {
        this.taskService = taskService;
        this.auditService = auditService;
    }

    @PostMapping
    @Operation(summary = "Criar uma tarefa")
    public ResponseEntity<TaskResponse> create(@PathVariable UUID projectId,
                                               @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.create(projectId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar tarefas com filtros, paginação e ordenação")
    public PageResponse<TaskResponse> list(@PathVariable UUID projectId,
                                           @Valid @ParameterObject TaskFilterParams params) {
        return taskService.list(projectId, params);
    }

    @GetMapping("/search")
    @Operation(summary = "Pesquisar tarefas por título ou descrição")
    public PageResponse<TaskResponse> search(
            @PathVariable UUID projectId,
            @RequestParam("q") @NotBlank @Size(max = 200) String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return taskService.search(projectId, query, page, size);
    }

    @GetMapping("/summary")
    @Operation(summary = "Obter contadores de tarefas por status e prioridade")
    public TaskSummaryResponse summary(@PathVariable UUID projectId) {
        return taskService.summary(projectId);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Obter uma tarefa")
    public TaskResponse get(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        return taskService.get(projectId, taskId);
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Atualizar detalhes da tarefa e o responsável")
    public TaskResponse update(@PathVariable UUID projectId, @PathVariable UUID taskId,
                               @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(projectId, taskId, request);
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Alterar status da tarefa")
    public TaskResponse updateStatus(@PathVariable UUID projectId, @PathVariable UUID taskId,
                                     @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(projectId, taskId, request.status());
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir tarefa")
    public void delete(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        taskService.delete(projectId, taskId);
    }

    @GetMapping("/{taskId}/history")
    @Operation(summary = "Listar histórico de auditoria da tarefa")
    public PageResponse<TaskAuditResponse> history(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return auditService.list(projectId, taskId, page, size);
    }
}
