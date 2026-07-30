package com.caiowilquer.taskmanager.repository.projection;

import com.caiowilquer.taskmanager.entity.enums.TaskPriority;

public interface TaskPriorityCountProjection {
    TaskPriority getPriority();
    long getTotal();
}
