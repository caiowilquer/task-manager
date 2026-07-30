package com.caiowilquer.taskmanager.mapper;

import com.caiowilquer.taskmanager.dto.project.ProjectDetailsResponse;
import com.caiowilquer.taskmanager.dto.project.ProjectMemberResponse;
import com.caiowilquer.taskmanager.dto.project.ProjectResponse;
import com.caiowilquer.taskmanager.entity.Project;
import com.caiowilquer.taskmanager.entity.ProjectMember;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectMapper {

    private final UserMapper userMapper;

    public ProjectMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ProjectResponse toResponse(Project project, long memberCount) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(),
                userMapper.toResponse(project.getOwner()), memberCount, project.getVersion(),
                project.getCreatedAt(), project.getUpdatedAt());
    }

    public ProjectDetailsResponse toDetails(Project project, List<ProjectMember> members) {
        List<ProjectMemberResponse> memberResponses = members.stream()
                .map(member -> new ProjectMemberResponse(
                        userMapper.toResponse(member.getUser()),
                        member.getJoinedAt(),
                        member.getUser().getId().equals(project.getOwner().getId())))
                .toList();
        return new ProjectDetailsResponse(project.getId(), project.getName(), project.getDescription(),
                userMapper.toResponse(project.getOwner()), memberResponses, project.getVersion(),
                project.getCreatedAt(), project.getUpdatedAt());
    }
}
