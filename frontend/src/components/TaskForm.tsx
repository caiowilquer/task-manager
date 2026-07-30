import { useState, type FormEvent } from 'react';
import type { ProjectMember, Task, TaskPayload, TaskPriority } from '../types/api';

interface TaskFormProps {
  members: ProjectMember[];
  task?: Task;
  submitting?: boolean;
  onSubmit: (payload: TaskPayload) => void;
  onCancel: () => void;
}

export function TaskForm({ members, task, submitting, onSubmit, onCancel }: TaskFormProps) {
  const [title, setTitle] = useState(task?.title ?? '');
  const [description, setDescription] = useState(task?.description ?? '');
  const [priority, setPriority] = useState<TaskPriority>(task?.priority ?? 'MEDIUM');
  const [deadline, setDeadline] = useState(task?.deadline ?? '');
  const [assigneeId, setAssigneeId] = useState(task?.assignee.id ?? members[0]?.user.id ?? '');

  const submit = (event: FormEvent) => {
    event.preventDefault();
    onSubmit({
      title: title.trim(),
      description: description.trim() || undefined,
      priority,
      deadline: deadline || null,
      assigneeId,
    });
  };

  return (
    <form className="stack-form" onSubmit={submit} data-testid="task-form">
      <label>
        Título
        <input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={160} data-testid="task-title" />
      </label>
      <label>
        Descrição
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} maxLength={4000} rows={5} />
      </label>
      <div className="form-grid">
        <label>
          Prioridade
          <select value={priority} onChange={(e) => setPriority(e.target.value as TaskPriority)} data-testid="task-priority">
            <option value="LOW">Baixa</option>
            <option value="MEDIUM">Média</option>
            <option value="HIGH">Alta</option>
            <option value="CRITICAL">Crítica</option>
          </select>
        </label>
        <label>
          Prazo
          <input type="date" value={deadline} min={new Date().toISOString().slice(0, 10)} onChange={(e) => setDeadline(e.target.value)} />
        </label>
      </div>
      <label>
        Responsável
        <select value={assigneeId} onChange={(e) => setAssigneeId(e.target.value)} required data-testid="task-assignee">
          {members.map((member) => <option key={member.user.id} value={member.user.id}>{member.user.name}</option>)}
        </select>
      </label>
      <div className="form-actions">
        <button type="button" className="button secondary" onClick={onCancel}>Cancelar</button>
        <button type="submit" className="button primary" disabled={submitting || !title.trim() || !assigneeId} data-testid="save-task">
          {submitting ? 'Salvando...' : task ? 'Atualizar tarefa' : 'Criar tarefa'}
        </button>
      </div>
    </form>
  );
}
