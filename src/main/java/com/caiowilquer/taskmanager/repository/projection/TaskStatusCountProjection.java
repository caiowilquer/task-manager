package com.caiowilquer.taskmanager.repository.projection;

import com.caiowilquer.taskmanager.entity.enums.TaskStatus;

public interface TaskStatusCountProjection {
    TaskStatus getStatus();
    long getTotal();
}
