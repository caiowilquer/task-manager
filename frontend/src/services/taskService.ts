import { api } from '../lib/api';
import type { PageResponse, Task, TaskAudit, TaskFilters, TaskPayload, TaskStatus, TaskSummary } from '../types/api';

export const taskService = {
  async list(projectId: string, filters: TaskFilters = {}) {
    const { data } = await api.get<PageResponse<Task>>(`/projects/${projectId}/tasks`, { params: filters });
    return data;
  },
  async create(projectId: string, payload: TaskPayload) {
    const { data } = await api.post<Task>(`/projects/${projectId}/tasks`, payload);
    return data;
  },
  async update(projectId: string, taskId: string, payload: TaskPayload) {
    const { data } = await api.put<Task>(`/projects/${projectId}/tasks/${taskId}`, payload);
    return data;
  },
  async updateStatus(projectId: string, taskId: string, status: TaskStatus) {
    const { data } = await api.patch<Task>(`/projects/${projectId}/tasks/${taskId}/status`, { status });
    return data;
  },
  async remove(projectId: string, taskId: string) {
    await api.delete(`/projects/${projectId}/tasks/${taskId}`);
  },
  async summary(projectId: string) {
    const { data } = await api.get<TaskSummary>(`/projects/${projectId}/tasks/summary`);
    return data;
  },
  async history(projectId: string, taskId: string) {
    const { data } = await api.get<PageResponse<TaskAudit>>(
      `/projects/${projectId}/tasks/${taskId}/history`,
      { params: { page: 0, size: 100 } },
    );
    return data;
  },
};
