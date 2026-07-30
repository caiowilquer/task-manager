package com.caiowilquer.taskmanager.dto.task;

import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;

import java.util.Map;

public record TaskSummaryResponse(Map<TaskStatus, Long> byStatus, Map<TaskPriority, Long> byPriority) {
}
