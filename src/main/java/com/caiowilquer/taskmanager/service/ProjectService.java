package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.dto.common.PageResponse;
import com.caiowilquer.taskmanager.dto.project.AddProjectMemberRequest;
import com.caiowilquer.taskmanager.dto.project.CreateProjectRequest;
import com.caiowilquer.taskmanager.dto.project.ProjectDetailsResponse;
import com.caiowilquer.taskmanager.dto.project.ProjectMemberResponse;
import com.caiowilquer.taskmanager.dto.project.ProjectResponse;
import com.caiowilquer.taskmanager.dto.project.UpdateProjectRequest;
import com.caiowilquer.taskmanager.entity.Project;
import com.caiowilquer.taskmanager.entity.ProjectMember;
import com.caiowilquer.taskmanager.entity.User;
import com.caiowilquer.taskmanager.entity.enums.UserRole;
import com.caiowilquer.taskmanager.exception.BusinessRuleException;
import com.caiowilquer.taskmanager.exception.ConflictException;
import com.caiowilquer.taskmanager.exception.ResourceNotFoundException;
import com.caiowilquer.taskmanager.mapper.ProjectMapper;
import com.caiowilquer.taskmanager.mapper.UserMapper;
import com.caiowilquer.taskmanager.repository.ProjectMemberRepository;
import com.caiowilquer.taskmanager.repository.ProjectRepository;
import com.caiowilquer.taskmanager.repository.TaskRepository;
import com.caiowilquer.taskmanager.repository.UserRepository;
import com.caiowilquer.taskmanager.repository.projection.ProjectMemberCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectAccessService accessService;
    private final CurrentUserService currentUserService;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository,
                          UserRepository userRepository,
                          TaskRepository taskRepository,
                          ProjectAccessService accessService,
                          CurrentUserService currentUserService,
                          ProjectMapper projectMapper,
                          UserMapper userMapper) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.accessService = accessService;
        this.currentUserService = currentUserService;
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public ProjectDetailsResponse create(CreateProjectRequest request) {
        User owner = currentUserService.entity();
        if (owner.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Apenas usuários administradores podem criar projetos.");
        }
        Project project = projectRepository.save(Project.create(request.name(), request.description(), owner));
        memberRepository.save(ProjectMember.create(project, owner));
        return projectMapper.toDetails(project, memberRepository.findByProjectIdOrderByUserNameAsc(project.getId()));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> list(int page, int size, String sortBy, String direction) {
        String safeSort = switch (sortBy == null ? "createdAt" : sortBy) {
            case "name" -> "name";
            case "updatedAt" -> "updatedAt";
            default -> "createdAt";
        };
        Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<Project> projects = projectRepository.findAccessibleByUserId(currentUserService.id(),
                PageRequest.of(page, size, Sort.by(safeDirection, safeSort)));

        List<UUID> ids = projects.getContent().stream().map(Project::getId).toList();
        Map<UUID, Long> counts = new HashMap<>();
        if (!ids.isEmpty()) {
            for (ProjectMemberCountProjection projection : memberRepository.countMembersByProjectIds(ids)) {
                counts.put(projection.getProjectId(), projection.getMemberCount());
            }
        }
        return PageResponse.from(projects,
                project -> projectMapper.toResponse(project, counts.getOrDefault(project.getId(), 0L)));
    }

    @Transactional(readOnly = true)
    public ProjectDetailsResponse get(UUID projectId) {
        Project project = accessService.requireMembership(projectId);
        return projectMapper.toDetails(project, memberRepository.findByProjectIdOrderByUserNameAsc(projectId));
    }

    @Transactional
    public ProjectDetailsResponse update(UUID projectId, UpdateProjectRequest request) {
        Project project = accessService.requireOwner(projectId);
        project.update(request.name(), request.description());
        return projectMapper.toDetails(project, memberRepository.findByProjectIdOrderByUserNameAsc(projectId));
    }

    @Transactional
    public void delete(UUID projectId) {
        Project project = accessService.requireOwner(projectId);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID projectId, AddProjectMemberRequest request) {
        Project project = accessService.requireOwner(projectId);
        User user = userRepository.findByEmail(User.normalizeEmail(request.email()))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para o email informado."));
        if (!user.isActive()) {
            throw new BusinessRuleException("Não é possível adicionar um usuário inativo.");
        }
        if (memberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new ConflictException("O usuário já pertence ao projeto.");
        }
        ProjectMember membership = memberRepository.save(ProjectMember.create(project, user));
        return new ProjectMemberResponse(userMapper.toResponse(user), membership.getJoinedAt(), false);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(UUID projectId) {
        Project project = accessService.requireMembership(projectId);
        return memberRepository.findByProjectIdOrderByUserNameAsc(projectId).stream()
                .map(member -> new ProjectMemberResponse(userMapper.toResponse(member.getUser()), member.getJoinedAt(),
                        member.getUser().getId().equals(project.getOwner().getId())))
                .toList();
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        Project project = accessService.requireOwner(projectId);
        if (project.getOwner().getId().equals(userId)) {
            throw new BusinessRuleException("O dono do projeto não pode ser removido.");
        }
        ProjectMember membership = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado no projeto."));
        if (taskRepository.existsByProjectIdAndAssigneeId(projectId, userId)) {
            throw new BusinessRuleException("Reatribua as tarefas deste usuário antes de removê-lo do projeto.");
        }
        memberRepository.delete(membership);
    }
}
