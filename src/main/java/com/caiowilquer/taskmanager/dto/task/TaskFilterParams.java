package com.caiowilquer.taskmanager.dto.task;

import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TaskFilterParams {

    private TaskStatus status;
    private TaskPriority priority;
    private UUID assigneeId;
    private Instant createdFrom;
    private Instant createdTo;
    private LocalDate deadlineFrom;
    private LocalDate deadlineTo;

    @Size(max = 200, message = "O texto de pesquisa deve ter no máximo 200 caracteres.")
    private String query;

    @Min(value = 0, message = "A página deve ser zero ou maior")
    private int page = 0;

    @Min(value = 1, message = "O tamanho deve ser de pelo menos 1")
    @Max(value = 100, message = "O tamanho deve ser de, no máximo, 100.")
    private int size = 20;

    private TaskSortField sortBy = TaskSortField.CREATED_AT;
    private SortDirection direction = SortDirection.DESC;

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public UUID getAssigneeId() { return assigneeId; }
    public void setAssigneeId(UUID assigneeId) { this.assigneeId = assigneeId; }
    public Instant getCreatedFrom() { return createdFrom; }
    public void setCreatedFrom(Instant createdFrom) { this.createdFrom = createdFrom; }
    public Instant getCreatedTo() { return createdTo; }
    public void setCreatedTo(Instant createdTo) { this.createdTo = createdTo; }
    public LocalDate getDeadlineFrom() { return deadlineFrom; }
    public void setDeadlineFrom(LocalDate deadlineFrom) { this.deadlineFrom = deadlineFrom; }
    public LocalDate getDeadlineTo() { return deadlineTo; }
    public void setDeadlineTo(LocalDate deadlineTo) { this.deadlineTo = deadlineTo; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public TaskSortField getSortBy() { return sortBy; }
    public void setSortBy(TaskSortField sortBy) { this.sortBy = sortBy; }
    public SortDirection getDirection() { return direction; }
    public void setDirection(SortDirection direction) { this.direction = direction; }
}
