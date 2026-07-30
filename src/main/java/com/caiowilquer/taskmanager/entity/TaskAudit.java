package com.caiowilquer.taskmanager.entity;

import com.caiowilquer.taskmanager.entity.enums.AuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "task_audits")
@EntityListeners(AuditingEntityListener.class)
public class TaskAudit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    @Column(name = "field_name", length = 80)
    private String fieldName;

    @Column(name = "previous_value", length = 1000)
    private String previousValue;

    @Column(name = "new_value", length = 1000)
    private String newValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @CreatedDate
    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    protected TaskAudit() {
    }

    private TaskAudit(Task task, AuditAction action, String fieldName,
                      String previousValue, String newValue, User changedBy) {
        this.task = task;
        this.action = action;
        this.fieldName = fieldName;
        this.previousValue = truncate(previousValue);
        this.newValue = truncate(newValue);
        this.changedBy = changedBy;
    }

    public static TaskAudit create(Task task, AuditAction action, String fieldName,
                                   String previousValue, String newValue, User changedBy) {
        return new TaskAudit(task, action, fieldName, previousValue, newValue, changedBy);
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 997) + "...";
    }

    public Task getTask() {
        return task;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
