package com.caiowilquer.taskmanager.repository.projection;

import java.util.UUID;

public interface ProjectMemberCountProjection {
    UUID getProjectId();
    long getMemberCount();
}
