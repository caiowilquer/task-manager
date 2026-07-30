package com.caiowilquer.taskmanager.repository;

import com.caiowilquer.taskmanager.entity.TaskAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskAuditRepository extends JpaRepository<TaskAudit, UUID> {

    @EntityGraph(attributePaths = "changedBy")
    Page<TaskAudit> findByTaskId(UUID taskId, Pageable pageable);
}
