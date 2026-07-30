package com.caiowilquer.taskmanager.mapper;

import com.caiowilquer.taskmanager.dto.task.TaskAuditResponse;
import com.caiowilquer.taskmanager.dto.task.TaskResponse;
import com.caiowilquer.taskmanager.entity.Task;
import com.caiowilquer.taskmanager.entity.TaskAudit;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    private final UserMapper userMapper;

    public TaskMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getProject().getId(), task.getTitle(),
                task.getDescription(), task.getStatus(), task.getPriority(), task.getDeadline(),
                userMapper.toResponse(task.getAssignee()), userMapper.toResponse(task.getCreatedBy()),
                task.getVersion(), task.getCreatedAt(), task.getUpdatedAt());
    }

    public TaskAuditResponse toAuditResponse(TaskAudit audit) {
        return new TaskAuditResponse(audit.getId(), audit.getAction(), audit.getFieldName(),
                audit.getPreviousValue(), audit.getNewValue(), userMapper.toResponse(audit.getChangedBy()),
                audit.getChangedAt());
    }
}
