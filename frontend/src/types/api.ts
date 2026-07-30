export type UserRole = 'ADMIN' | 'MEMBER';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Project {
  id: string;
  name: string;
  description: string | null;
  owner: User;
  memberCount: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectMember {
  user: User;
  joinedAt: string;
  owner: boolean;
}

export interface ProjectDetails extends Omit<Project, 'memberCount'> {
  members: ProjectMember[];
}

export interface Task {
  id: string;
  projectId: string;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  deadline: string | null;
  assignee: User;
  createdBy: User;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface TaskSummary {
  byStatus: Record<TaskStatus, number>;
  byPriority: Record<TaskPriority, number>;
}

export type TaskAuditAction = 'CREATED' | 'UPDATED' | 'STATUS_CHANGED' | 'ASSIGNEE_CHANGED';

export interface TaskAudit {
  id: string;
  action: TaskAuditAction;
  fieldName: string | null;
  previousValue: string | null;
  newValue: string | null;
  changedBy: User;
  changedAt: string;
}

export interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string>;
}

export interface TaskPayload {
  title: string;
  description?: string;
  priority: TaskPriority;
  deadline?: string | null;
  assigneeId: string;
}

export interface TaskFilters {
  status?: TaskStatus;
  priority?: TaskPriority;
  assigneeId?: string;
  query?: string;
  deadlineFrom?: string;
  deadlineTo?: string;
  sortBy?: 'PRIORITY' | 'CREATED_AT' | 'DEADLINE';
  direction?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}
