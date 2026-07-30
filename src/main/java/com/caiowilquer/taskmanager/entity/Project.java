package com.caiowilquer.taskmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "projects")
public class Project extends AuditableEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Version
    @Column(nullable = false)
    private long version;

    protected Project() {
    }

    private Project(String name, String description, User owner) {
        this.name = name.trim();
        this.description = normalizeDescription(description);
        this.owner = owner;
    }

    public static Project create(String name, String description, User owner) {
        return new Project(name, description, owner);
    }

    public void update(String name, String description) {
        this.name = name.trim();
        this.description = normalizeDescription(description);
    }

    private static String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public User getOwner() {
        return owner;
    }

    public long getVersion() {
        return version;
    }
}
