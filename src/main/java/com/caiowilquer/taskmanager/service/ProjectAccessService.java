package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.entity.Project;
import com.caiowilquer.taskmanager.exception.ResourceNotFoundException;
import com.caiowilquer.taskmanager.repository.ProjectMemberRepository;
import com.caiowilquer.taskmanager.repository.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProjectAccessService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final CurrentUserService currentUserService;

    public ProjectAccessService(ProjectRepository projectRepository,
                                ProjectMemberRepository memberRepository,
                                CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public Project requireMembership(UUID projectId) {
        UUID userId = currentUserService.id();
        Project project = findProject(projectId);
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new AccessDeniedException("O usuário não é membro do projeto");
        }
        return project;
    }

    @Transactional(readOnly = true)
    public Project requireOwner(UUID projectId) {
        Project project = findProject(projectId);
        if (!project.getOwner().getId().equals(currentUserService.id())) {
            throw new AccessDeniedException("Apenas o proprietário do projeto pode realizar esta operação.");
        }
        return project;
    }

    @Transactional(readOnly = true)
    public Project findProject(UUID projectId) {
        return projectRepository.findDetailedById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado."));
    }
}
