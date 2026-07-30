import { api } from '../lib/api';
import type { PageResponse, Project, ProjectDetails, ProjectMember } from '../types/api';

export const projectService = {
  async list(page = 0, size = 20) {
    const { data } = await api.get<PageResponse<Project>>('/projects', { params: { page, size } });
    return data;
  },
  async get(id: string) {
    const { data } = await api.get<ProjectDetails>(`/projects/${id}`);
    return data;
  },
  async create(payload: { name: string; description?: string }) {
    const { data } = await api.post<ProjectDetails>('/projects', payload);
    return data;
  },
  async update(id: string, payload: { name: string; description?: string }) {
    const { data } = await api.put<ProjectDetails>(`/projects/${id}`, payload);
    return data;
  },
  async remove(id: string) {
    await api.delete(`/projects/${id}`);
  },
  async addMember(id: string, email: string) {
    const { data } = await api.post<ProjectMember>(`/projects/${id}/members`, { email });
    return data;
  },
  async removeMember(id: string, userId: string) {
    await api.delete(`/projects/${id}/members/${userId}`);
  },
};
