package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.dto.common.PageResponse;
import com.caiowilquer.taskmanager.dto.task.TaskAuditResponse;
import com.caiowilquer.taskmanager.entity.Task;
import com.caiowilquer.taskmanager.entity.TaskAudit;
import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.AuditAction;
import com.caiowilquer.taskmanager.mapper.TaskMapper;
import com.caiowilquer.taskmanager.repository.TaskAuditRepository;
import com.caiowilquer.taskmanager.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskAuditService {

    private final TaskAuditRepository auditRepository;
    private final TaskRepository taskRepository;
    private final ProjectAccessService accessService;
    private final TaskMapper taskMapper;

    public TaskAuditService(TaskAuditRepository auditRepository,
                            TaskRepository taskRepository,
                            ProjectAccessService accessService,
                            TaskMapper taskMapper) {
        this.auditRepository = auditRepository;
        this.taskRepository = taskRepository;
        this.accessService = accessService;
        this.taskMapper = taskMapper;
    }

    public void recordCreated(Task task, User user) {
        auditRepository.save(TaskAudit.create(task, AuditAction.CREATED, "task", null, task.getTitle(), user));
    }

    public void recordDetailsChanges(Task task, User user,
                                     String oldTitle, String oldDescription, Object oldPriority,
                                     Object oldDeadline, User oldAssignee) {
        List<TaskAudit> changes = new ArrayList<>();
        addIfChanged(changes, task, user, AuditAction.UPDATED, "Título", oldTitle, task.getTitle());
        addIfChanged(changes, task, user, AuditAction.UPDATED, "Descrição", oldDescription, task.getDescription());
        addIfChanged(changes, task, user, AuditAction.UPDATED, "Prioridade", oldPriority, task.getPriority());
        addIfChanged(changes, task, user, AuditAction.UPDATED, "Prazo", oldDeadline, task.getDeadline());
        if (!oldAssignee.getId().equals(task.getAssignee().getId())) {
            changes.add(TaskAudit.create(task, AuditAction.ASSIGNEE_CHANGED, "assignee",
                    oldAssignee.getEmail(), task.getAssignee().getEmail(), user));
        }
        auditRepository.saveAll(changes);
    }

    public void recordStatusChange(Task task, User user, Object previousStatus) {
        auditRepository.save(TaskAudit.create(task, AuditAction.STATUS_CHANGED, "status",
                value(previousStatus), value(task.getStatus()), user));
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskAuditResponse> list(UUID projectId, UUID taskId, int page, int size) {
        accessService.requireMembership(projectId);
        taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new com.caiowilquer.taskmanager.exception.ResourceNotFoundException("Tarefa não encontrada."));
        return PageResponse.from(auditRepository.findByTaskId(taskId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "changedAt"))),
                taskMapper::toAuditResponse);
    }

    private void addIfChanged(List<TaskAudit> changes, Task task, User user, AuditAction action,
                              String field, Object previous, Object current) {
        if (!Objects.equals(previous, current)) {
            changes.add(TaskAudit.create(task, action, field, value(previous), value(current), user));
        }
    }

    private String value(Object value) {
        return value == null ? null : value.toString();
    }
}
