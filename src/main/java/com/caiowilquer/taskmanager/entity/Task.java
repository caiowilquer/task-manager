package com.caiowilquer.taskmanager.entity;

import com.caiowilquer.taskmanager.entity.enums.TaskPriority;
import com.caiowilquer.taskmanager.entity.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority;

    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    @Version
    @Column(nullable = false)
    private long version;

    protected Task() {
    }

    private Task(Project project, String title, String description, TaskPriority priority,
                 LocalDate deadline, User assignee, User createdBy) {
        this.project = project;
        this.title = title.trim();
        this.description = normalizeDescription(description);
        this.status = TaskStatus.TODO;
        this.priority = priority;
        this.deadline = deadline;
        this.assignee = assignee;
        this.createdBy = createdBy;
    }

    public static Task create(Project project, String title, String description, TaskPriority priority,
                              LocalDate deadline, User assignee, User createdBy) {
        return new Task(project, title, description, priority, deadline, assignee, createdBy);
    }

    public void updateDetails(String title, String description, TaskPriority priority, LocalDate deadline) {
        this.title = title.trim();
        this.description = normalizeDescription(description);
        this.priority = priority;
        this.deadline = deadline;
    }

    public void assignTo(User assignee) {
        this.assignee = assignee;
    }

    public void changeStatus(TaskStatus status) {
        this.status = status;
    }

    private static String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    public Project getProject() {
        return project;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public User getAssignee() {
        return assignee;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public long getVersion() {
        return version;
    }
}
