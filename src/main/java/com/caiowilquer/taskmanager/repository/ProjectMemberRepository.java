package com.caiowilquer.taskmanager.repository;

import com.caiowilquer.taskmanager.entity.ProjectMember;
import com.caiowilquer.taskmanager.repository.projection.ProjectMemberCountProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    @EntityGraph(attributePaths = "user")
    List<ProjectMember> findByProjectIdOrderByUserNameAsc(UUID projectId);

    long countByProjectId(UUID projectId);

    @Query("select pm.project.id as projectId, count(pm) as memberCount from ProjectMember pm " +
            "where pm.project.id in :projectIds group by pm.project.id")
    List<ProjectMemberCountProjection> countMembersByProjectIds(@Param("projectIds") Collection<UUID> projectIds);
}
