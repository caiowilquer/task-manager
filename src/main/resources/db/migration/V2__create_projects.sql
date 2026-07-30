CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    owner_id UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE project_members (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_project_members_project_user UNIQUE (project_id, user_id)
);

CREATE INDEX idx_projects_owner ON projects (owner_id);
CREATE INDEX idx_project_members_user ON project_members (user_id, project_id);
