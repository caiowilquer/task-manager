import { useEffect, useRef } from 'react';
import toast from 'react-hot-toast';
import type { Task } from '../types/api';
import { useAuthStore } from '../store/authStore';

export function useAssignmentNotifications(tasks: Task[]) {
  const user = useAuthStore((state) => state.user);
  const previous = useRef<Set<string> | null>(null);

  useEffect(() => {
    if (!user) return;
    const current = new Set(tasks.filter((task) => task.assignee.id === user.id).map((task) => task.id));
    if (previous.current) {
      tasks.filter((task) => task.assignee.id === user.id && !previous.current?.has(task.id))
        .forEach((task) => toast(`A tarefa “${task.title}” foi atribuída a você.`, { icon: '👤' }));
    }
    previous.current = current;
  }, [tasks, user]);
}
