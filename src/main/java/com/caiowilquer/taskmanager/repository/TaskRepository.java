package com.caiowilquer.taskmanager.repository;

import com.caiowilquer.taskmanager.entity.Task;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;
import com.caiowilquer.taskmanager.repository.custom.TaskQueryRepository;
import com.caiowilquer.taskmanager.repository.projection.TaskPriorityCountProjection;
import com.caiowilquer.taskmanager.repository.projection.TaskStatusCountProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, TaskQueryRepository {

    @EntityGraph(attributePaths = {"project", "project.owner", "assignee", "createdBy"})
    Optional<Task> findByIdAndProjectId(UUID id, UUID projectId);

    long countByAssigneeIdAndStatus(UUID assigneeId, TaskStatus status);

    long countByAssigneeIdAndStatusAndIdNot(UUID assigneeId, TaskStatus status, UUID id);

    boolean existsByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId);

    @Query("select t.status as status, count(t) as total from Task t " +
            "where t.project.id = :projectId group by t.status")
    List<TaskStatusCountProjection> countGroupedByStatus(@Param("projectId") UUID projectId);

    @Query("select t.priority as priority, count(t) as total from Task t " +
            "where t.project.id = :projectId group by t.priority")
    List<TaskPriorityCountProjection> countGroupedByPriority(@Param("projectId") UUID projectId);
}
