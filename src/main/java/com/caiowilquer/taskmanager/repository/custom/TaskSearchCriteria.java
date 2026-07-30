package com.caiowilquer.taskmanager.repository.custom;

import com.caiowilquer.taskmanager.dto.task.SortDirection;
import com.caiowilquer.taskmanager.dto.task.TaskSortField;
import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskSearchCriteria(
        TaskStatus status,
        TaskPriority priority,
        UUID assigneeId,
        Instant createdFrom,
        Instant createdTo,
        LocalDate deadlineFrom,
        LocalDate deadlineTo,
        String query,
        TaskSortField sortBy,
        SortDirection direction
) {
}
