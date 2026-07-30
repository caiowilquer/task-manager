package com.caiowilquer.taskmanager.repository.custom;

import com.caiowilquer.taskmanager.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskQueryRepository {
    Page<Task> search(UUID projectId, TaskSearchCriteria criteria, Pageable pageable);
}
