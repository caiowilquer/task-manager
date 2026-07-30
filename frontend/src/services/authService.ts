import { api } from '../lib/api';
import type { AuthResponse, User } from '../types/api';

export const authService = {
  async login(email: string, password: string) {
    const { data } = await api.post<AuthResponse>('/auth/login', { email, password });
    return data;
  },
  async register(name: string, email: string, password: string) {
    const { data } = await api.post<User>('/auth/register', { name, email, password });
    return data;
  },
  async me() {
    const { data } = await api.get<User>('/auth/me');
    return data;
  },
};
