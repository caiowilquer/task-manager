export type TaskAuditAction = 'CREATED' | 'UPDATED' | 'STATUS_CHANGED' | 'ASSIGNEE_CHANGED';

const taskAuditActionLabels: Record<TaskAuditAction, string> = {
  CREATED: 'Criado',
  UPDATED: 'Atualizado',
  STATUS_CHANGED: 'Status alterado',
  ASSIGNEE_CHANGED: 'Responsável alterado',
};

const taskAuditFieldLabels: Record<string, string> = {
  status: 'Status',
  priority: 'Prioridade',
  assignee: 'Responsável',
  title: 'Título',
  description: 'Descrição',
  deadline: 'Prazo',
};

const taskStatusLabels: Record<string, string> = {
  TODO: 'A fazer',
  IN_PROGRESS: 'Em andamento',
  DONE: 'Concluída',
};

const taskPriorityLabels: Record<string, string> = {
  LOW: 'Baixa',
  MEDIUM: 'Média',
  HIGH: 'Alta',
  CRITICAL: 'Crítica',
};

export function formatTaskAuditAction(action: TaskAuditAction): string {
  return taskAuditActionLabels[action] ?? action;
}

export function formatTaskAuditFieldName(fieldName: string | null | undefined): string {
  if (!fieldName) return 'Campo';
  return taskAuditFieldLabels[fieldName] ?? fieldName;
}

export function formatTaskAuditValue(fieldName: string | null | undefined, value: string | null | undefined): string {
  if (!value) return '—';

  if (fieldName === 'status') {
    return taskStatusLabels[value] ?? value;
  }

  if (fieldName === 'priority') {
    return taskPriorityLabels[value] ?? value;
  }

  return value;
}
