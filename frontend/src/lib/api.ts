import axios from 'axios';
import { useAuthStore } from '../store/authStore';
import type { ProblemDetail } from '../types/api';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api/v1',
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) useAuthStore.getState().clearSession();
    return Promise.reject(error);
  },
);

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ProblemDetail>(error)) {
    const data = error.response?.data;
    if (data?.errors) return Object.values(data.errors).join(' ');
    return data?.detail ?? 'Não foi possível concluir a operação.';
  }
  return error instanceof Error ? error.message : 'Erro inesperado.';
}
