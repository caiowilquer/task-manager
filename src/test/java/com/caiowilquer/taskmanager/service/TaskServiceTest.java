package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.entity.Project;
import com.caiowilquer.taskmanager.entity.Task;
import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;
import com.caiowilquer.taskmanager.entity.enums.UserRole;
import com.caiowilquer.taskmanager.exception.BusinessRuleException;
import com.caiowilquer.taskmanager.mapper.TaskMapper;
import com.caiowilquer.taskmanager.repository.ProjectMemberRepository;
import com.caiowilquer.taskmanager.repository.TaskRepository;
import com.caiowilquer.taskmanager.repository.UserRepository;
import com.caiowilquer.taskmanager.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository memberRepository;

    @Mock
    private ProjectAccessService accessService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TaskAuditService auditService;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskSummaryService taskSummaryService;

    private TaskService service;
    private UUID projectId;
    private UUID taskId;
    private User owner;
    private User assignee;
    private Project project;

    @BeforeEach
    void setUp() {
        service = new TaskService(
                taskRepository,
                userRepository,
                memberRepository,
                accessService,
                currentUserService,
                auditService,
                taskMapper,
                taskSummaryService
        );

        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        owner = user(
                "Owner",
                "owner@test.local",
                UserRole.ADMIN
        );

        assignee = user(
                "Member",
                "member@test.local",
                UserRole.MEMBER
        );

        project = Project.create(
                "Project",
                null,
                owner
        );

        ReflectionTestUtils.setField(
                project,
                "id",
                projectId
        );
    }

    @Test
    void shouldRejectDoneToTodoTransition() {
        Task task = task(
                TaskPriority.HIGH,
                TaskStatus.DONE
        );

        when(accessService.requireMembership(projectId))
                .thenReturn(project);

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        assertThatThrownBy(() ->
                service.updateStatus(
                        projectId,
                        taskId,
                        TaskStatus.TODO
                )
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(
                        "não pode voltar diretamente para A FAZER"
                );

        verifyNoInteractions(auditService);
    }

    @Test
    void shouldRejectCriticalCompletionByMember() {
        Task task = task(
                TaskPriority.CRITICAL,
                TaskStatus.IN_PROGRESS
        );

        UserPrincipal principal = mock(UserPrincipal.class);

        when(principal.getRole())
                .thenReturn(UserRole.MEMBER);

        when(accessService.requireMembership(projectId))
                .thenReturn(project);

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        when(currentUserService.principal())
                .thenReturn(principal);

        assertThatThrownBy(() ->
                service.updateStatus(
                        projectId,
                        taskId,
                        TaskStatus.DONE
                )
        )
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(auditService);
    }

    @Test
    void shouldRejectSixthInProgressTask() {
        Task task = task(
                TaskPriority.MEDIUM,
                TaskStatus.TODO
        );

        when(accessService.requireMembership(projectId))
                .thenReturn(project);

        when(taskRepository.findByIdAndProjectId(taskId, projectId))
                .thenReturn(Optional.of(task));

        when(userRepository.findByIdForUpdate(assignee.getId()))
                .thenReturn(Optional.of(assignee));

        when(taskRepository.countByAssigneeIdAndStatusAndIdNot(
                assignee.getId(),
                TaskStatus.IN_PROGRESS,
                taskId
        )).thenReturn(5L);

        assertThatThrownBy(() ->
                service.updateStatus(
                        projectId,
                        taskId,
                        TaskStatus.IN_PROGRESS
                )
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(
                        "5 tarefas EM ANDAMENTO"
                );

        verifyNoInteractions(auditService);
    }

    private User user(
            String name,
            String email,
            UserRole role
    ) {
        User user = User.create(
                name,
                email,
                "hash",
                role
        );

        ReflectionTestUtils.setField(
                user,
                "id",
                UUID.randomUUID()
        );

        return user;
    }

    private Task task(
            TaskPriority priority,
            TaskStatus status
    ) {
        Task task = Task.create(
                project,
                "Task",
                "Description",
                priority,
                LocalDate.now().plusDays(3),
                assignee,
                owner
        );

        task.changeStatus(status);

        ReflectionTestUtils.setField(
                task,
                "id",
                taskId
        );

        return task;
    }
}