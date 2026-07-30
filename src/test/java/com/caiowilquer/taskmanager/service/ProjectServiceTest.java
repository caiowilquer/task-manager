package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.dto.project.AddProjectMemberRequest;
import com.caiowilquer.taskmanager.dto.project.CreateProjectRequest;
import com.caiowilquer.taskmanager.entity.Project;
import com.caiowilquer.taskmanager.entity.ProjectMember;
import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.UserRole;
import com.caiowilquer.taskmanager.exception.BusinessRuleException;
import com.caiowilquer.taskmanager.exception.ConflictException;
import com.caiowilquer.taskmanager.mapper.ProjectMapper;
import com.caiowilquer.taskmanager.mapper.UserMapper;
import com.caiowilquer.taskmanager.repository.ProjectMemberRepository;
import com.caiowilquer.taskmanager.repository.ProjectRepository;
import com.caiowilquer.taskmanager.repository.TaskRepository;
import com.caiowilquer.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock ProjectMemberRepository memberRepository;
    @Mock UserRepository userRepository;
    @Mock TaskRepository taskRepository;
    @Mock ProjectAccessService accessService;
    @Mock CurrentUserService currentUserService;
    @Mock ProjectMapper projectMapper;
    @Mock UserMapper userMapper;

    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(projectRepository, memberRepository, userRepository,
                taskRepository, accessService, currentUserService, projectMapper, userMapper);
    }

    @Test
    void shouldRejectProjectCreationByMember() {
        User member = user("Member", "member@test.local", UserRole.MEMBER);
        when(currentUserService.entity()).thenReturn(member);

        assertThatThrownBy(() -> service.create(new CreateProjectRequest("Project", null)))
                .isInstanceOf(AccessDeniedException.class);
        verify(projectRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicatedProjectMember() {
        UUID projectId = UUID.randomUUID();
        User owner = user("Owner", "owner@test.local", UserRole.ADMIN);
        User member = user("Member", "member@test.local", UserRole.MEMBER);
        Project project = Project.create("Project", null, owner);
        when(accessService.requireOwner(projectId)).thenReturn(project);
        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(memberRepository.existsByProjectIdAndUserId(projectId, member.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.addMember(projectId, new AddProjectMemberRequest(member.getEmail())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("já pertence");
    }

    @Test
    void shouldRequireTaskReassignmentBeforeRemovingMember() {
        UUID projectId = UUID.randomUUID();
        User owner = user("Owner", "owner@test.local", UserRole.ADMIN);
        User member = user("Member", "member@test.local", UserRole.MEMBER);
        Project project = Project.create("Project", null, owner);
        ProjectMember membership = ProjectMember.create(project, member);
        when(accessService.requireOwner(projectId)).thenReturn(project);
        when(memberRepository.findByProjectIdAndUserId(projectId, member.getId()))
                .thenReturn(Optional.of(membership));
        when(taskRepository.existsByProjectIdAndAssigneeId(projectId, member.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.removeMember(projectId, member.getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Reatribua");
        verify(memberRepository, never()).delete(any());
    }

    private User user(String name, String email, UserRole role) {
        User user = User.create(name, email, "hash", role);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
