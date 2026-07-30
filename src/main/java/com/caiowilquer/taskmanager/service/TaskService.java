package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.config.CacheConfig;
import com.caiowilquer.taskmanager.dto.common.PageResponse;
import com.caiowilquer.taskmanager.dto.task.CreateTaskRequest;
import com.caiowilquer.taskmanager.dto.task.TaskFilterParams;
import com.caiowilquer.taskmanager.dto.task.TaskResponse;
import com.caiowilquer.taskmanager.dto.task.TaskSummaryResponse;
import com.caiowilquer.taskmanager.dto.task.UpdateTaskRequest;
import com.caiowilquer.taskmanager.entity.Project;
import com.caiowilquer.taskmanager.entity.Task;
import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;
import com.caiowilquer.taskmanager.entity.enums.UserRole;
import com.caiowilquer.taskmanager.exception.BusinessRuleException;
import com.caiowilquer.taskmanager.exception.ResourceNotFoundException;
import com.caiowilquer.taskmanager.mapper.TaskMapper;
import com.caiowilquer.taskmanager.repository.ProjectMemberRepository;
import com.caiowilquer.taskmanager.repository.TaskRepository;
import com.caiowilquer.taskmanager.repository.UserRepository;
import com.caiowilquer.taskmanager.repository.custom.TaskSearchCriteria;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TaskService {

    public static final int WIP_LIMIT = 5;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectAccessService accessService;
    private final CurrentUserService currentUserService;
    private final TaskAuditService auditService;
    private final TaskMapper taskMapper;
    private final TaskSummaryService taskSummaryService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       ProjectMemberRepository memberRepository,
                       ProjectAccessService accessService,
                       CurrentUserService currentUserService,
                       TaskAuditService auditService,
                       TaskMapper taskMapper,
                       TaskSummaryService taskSummaryService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.accessService = accessService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.taskMapper = taskMapper;
        this.taskSummaryService = taskSummaryService;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.PROJECT_SUMMARY_CACHE, key = "#projectId")
    public TaskResponse create(UUID projectId, CreateTaskRequest request) {
        Project project = accessService.requireMembership(projectId);
        User assignee = requireProjectMember(projectId, request.assigneeId());
        User creator = currentUserService.entity();
        Task task = taskRepository.save(Task.create(project, request.title(), request.description(),
                request.priority(), request.deadline(), assignee, creator));
        auditService.recordCreated(task, creator);
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(UUID projectId, UUID taskId) {
        accessService.requireMembership(projectId);
        return taskMapper.toResponse(findTask(projectId, taskId));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.PROJECT_SUMMARY_CACHE, key = "#projectId")
    public TaskResponse update(UUID projectId, UUID taskId, UpdateTaskRequest request) {
        accessService.requireMembership(projectId);
        Task task = findTask(projectId, taskId);
        User editor = currentUserService.entity();
        User newAssignee = requireProjectMember(projectId, request.assigneeId());

        String oldTitle = task.getTitle();
        String oldDescription = task.getDescription();
        TaskPriority oldPriority = task.getPriority();
        var oldDeadline = task.getDeadline();
        User oldAssignee = task.getAssignee();

        if (task.getStatus() == TaskStatus.IN_PROGRESS
                && !oldAssignee.getId().equals(newAssignee.getId())) {
            validateWipLimit(newAssignee.getId(), task.getId());
        }

        task.updateDetails(request.title(), request.description(), request.priority(), request.deadline());
        task.assignTo(newAssignee);
        auditService.recordDetailsChanges(task, editor, oldTitle, oldDescription, oldPriority,
                oldDeadline, oldAssignee);
        return taskMapper.toResponse(task);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.PROJECT_SUMMARY_CACHE, key = "#projectId")
    public TaskResponse updateStatus(UUID projectId, UUID taskId, TaskStatus requestedStatus) {
        Project project = accessService.requireMembership(projectId);
        Task task = findTask(projectId, taskId);
        TaskStatus currentStatus = task.getStatus();
        if (currentStatus == requestedStatus) {
            return taskMapper.toResponse(task);
        }

        validateTransition(currentStatus, requestedStatus);
        if (requestedStatus == TaskStatus.IN_PROGRESS) {
            validateWipLimit(task.getAssignee().getId(), task.getId());
        }
        if (task.getPriority() == TaskPriority.CRITICAL && requestedStatus == TaskStatus.DONE) {
            var principal = currentUserService.principal();
            boolean isProjectAdmin = principal.getRole() == UserRole.ADMIN
                    && project.getOwner().getId().equals(principal.getId());
            if (!isProjectAdmin) {
                throw new AccessDeniedException("Apenas o administrador do projeto pode concluir uma tarefa crítica.");
            }
        }

        User editor = currentUserService.entity();
        task.changeStatus(requestedStatus);
        auditService.recordStatusChange(task, editor, currentStatus);
        return taskMapper.toResponse(task);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.PROJECT_SUMMARY_CACHE, key = "#projectId")
    public void delete(UUID projectId, UUID taskId) {
        accessService.requireMembership(projectId);
        taskRepository.delete(findTask(projectId, taskId));
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> list(UUID projectId, TaskFilterParams params) {
        accessService.requireMembership(projectId);
        validateRanges(params);
        TaskSearchCriteria criteria = new TaskSearchCriteria(params.getStatus(), params.getPriority(),
                params.getAssigneeId(), params.getCreatedFrom(), params.getCreatedTo(),
                params.getDeadlineFrom(), params.getDeadlineTo(), params.getQuery(),
                params.getSortBy(), params.getDirection());
        Page<Task> tasks = taskRepository.search(projectId, criteria,
                PageRequest.of(params.getPage(), params.getSize()));
        return PageResponse.from(tasks, taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> search(UUID projectId, String query, int page, int size) {
        TaskFilterParams params = new TaskFilterParams();
        params.setQuery(query);
        params.setPage(page);
        params.setSize(size);
        return list(projectId, params);
    }

    @Transactional(readOnly = true)
    public TaskSummaryResponse summary(UUID projectId) {
        accessService.requireMembership(projectId);
        return taskSummaryService.calculate(projectId);
    }

    private Task findTask(UUID projectId, UUID taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada."));
    }

    private User requireProjectMember(UUID projectId, UUID userId) {
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessRuleException("O responsável deve ser membro do projeto.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não encontrado."));
    }

    private void validateTransition(TaskStatus current, TaskStatus requested) {
        if (current == TaskStatus.DONE && requested == TaskStatus.TODO) {
            throw new BusinessRuleException("Uma tarefa Finalizada não pode voltar diretamente para A FAZER. Use EM ANDAMENTO.");
        }
    }

    private void validateWipLimit(UUID assigneeId, UUID currentTaskId) {
        userRepository.findByIdForUpdate(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não encontrado."));
        long currentWip = taskRepository.countByAssigneeIdAndStatusAndIdNot(
                assigneeId, TaskStatus.IN_PROGRESS, currentTaskId);
        if (currentWip >= WIP_LIMIT) {
            throw new BusinessRuleException("Limite de tarefas atingido: o responsável já possui 5 tarefas EM ANDAMENTO.");
        }
    }

    private void validateRanges(TaskFilterParams params) {
        if (params.getCreatedFrom() != null && params.getCreatedTo() != null
                && params.getCreatedFrom().isAfter(params.getCreatedTo())) {
            throw new BusinessRuleException("A data inicial de criação não pode ser posterior à data final de criação.");
        }
        if (params.getDeadlineFrom() != null && params.getDeadlineTo() != null
                && params.getDeadlineFrom().isAfter(params.getDeadlineTo())) {
            throw new BusinessRuleException("A data inicial do prazo não pode ser posterior à data final do prazo.");
        }
    }
}
