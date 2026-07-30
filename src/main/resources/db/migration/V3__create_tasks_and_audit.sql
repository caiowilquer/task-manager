CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(4000),
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    deadline DATE,
    assignee_id UUID NOT NULL,
    created_by_id UUID NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users(id),
    CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT ck_tasks_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    CONSTRAINT ck_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE TABLE task_audits (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    action VARCHAR(30) NOT NULL,
    field_name VARCHAR(80),
    previous_value VARCHAR(1000),
    new_value VARCHAR(1000),
    changed_by_id UUID NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_task_audits_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_audits_changed_by FOREIGN KEY (changed_by_id) REFERENCES users(id),
    CONSTRAINT ck_task_audits_action CHECK (action IN ('CREATED', 'UPDATED', 'STATUS_CHANGED', 'ASSIGNEE_CHANGED'))
);

CREATE INDEX idx_tasks_project_status ON tasks (project_id, status);
CREATE INDEX idx_tasks_project_priority ON tasks (project_id, priority);
CREATE INDEX idx_tasks_project_assignee_status ON tasks (project_id, assignee_id, status);
CREATE INDEX idx_tasks_project_created_at ON tasks (project_id, created_at DESC);
CREATE INDEX idx_tasks_project_deadline ON tasks (project_id, deadline);
CREATE INDEX idx_tasks_title_trgm ON tasks USING GIN (LOWER(title) gin_trgm_ops);
CREATE INDEX idx_tasks_description_trgm ON tasks USING GIN (LOWER(description) gin_trgm_ops);
CREATE INDEX idx_task_audits_task_changed_at ON task_audits (task_id, changed_at DESC);
