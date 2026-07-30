package com.caiowilquer.taskmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "project_members")
@EntityListeners(AuditingEntityListener.class)
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected ProjectMember() {
    }

    private ProjectMember(Project project, User user) {
        this.project = project;
        this.user = user;
    }

    public static ProjectMember create(Project project, User user) {
        return new ProjectMember(project, user);
    }

    public Project getProject() {
        return project;
    }

    public User getUser() {
        return user;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
