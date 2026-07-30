package com.caiowilquer.taskmanager.service;

import com.caiowilquer.taskmanager.config.CacheConfig;
import com.caiowilquer.taskmanager.dto.task.TaskSummaryResponse;
import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;
import com.caiowilquer.taskmanager.repository.TaskRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskSummaryService {

    private final TaskRepository taskRepository;

    public TaskSummaryService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.PROJECT_SUMMARY_CACHE, key = "#projectId")
    public TaskSummaryResponse calculate(UUID projectId) {
        Map<TaskStatus, Long> byStatus = new EnumMap<>(TaskStatus.class);
        Arrays.stream(TaskStatus.values()).forEach(status -> byStatus.put(status, 0L));
        taskRepository.countGroupedByStatus(projectId)
                .forEach(row -> byStatus.put(row.getStatus(), row.getTotal()));

        Map<TaskPriority, Long> byPriority = new EnumMap<>(TaskPriority.class);
        Arrays.stream(TaskPriority.values()).forEach(priority -> byPriority.put(priority, 0L));
        taskRepository.countGroupedByPriority(projectId)
                .forEach(row -> byPriority.put(row.getPriority(), row.getTotal()));
        return new TaskSummaryResponse(Map.copyOf(byStatus), Map.copyOf(byPriority));
    }
}
