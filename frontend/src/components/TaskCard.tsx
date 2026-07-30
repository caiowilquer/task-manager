import { useDraggable } from '@dnd-kit/core';
import { CalendarDays, History, Pencil, Trash2, UserRound } from 'lucide-react';
import type { Task } from '../types/api';
import { PriorityBadge } from './PriorityBadge';

interface TaskCardProps {
  task: Task;
  onEdit: (task: Task) => void;
  onDelete: (task: Task) => void;
  onHistory: (task: Task) => void;
}

export function TaskCard({ task, onEdit, onDelete, onHistory }: TaskCardProps) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({ id: task.id, data: { task } });
  const style = transform ? { transform: `translate3d(${transform.x}px, ${transform.y}px, 0)` } : undefined;

  return (
    <article ref={setNodeRef} style={style} className={`task-card ${isDragging ? 'dragging' : ''}`} data-testid={`task-${task.id}`}>
      <div className="task-drag-area" {...listeners} {...attributes} title="Arraste para alterar o status">
        <div className="task-card-top">
          <PriorityBadge priority={task.priority} />
          <span className="task-id">#{task.id.slice(0, 6)}</span>
        </div>
        <h3>{task.title}</h3>
        {task.description && <p>{task.description}</p>}
        <div className="task-meta">
          <span><UserRound size={14} />{task.assignee.name}</span>
          {task.deadline && <span><CalendarDays size={14} />{new Date(`${task.deadline}T00:00:00`).toLocaleDateString('pt-BR')}</span>}
        </div>
      </div>
      <div className="task-actions">
        <button onClick={() => onHistory(task)} aria-label="Histórico" title="Histórico"><History size={16} /></button>
        <button onClick={() => onEdit(task)} aria-label="Editar" title="Editar"><Pencil size={16} /></button>
        <button onClick={() => onDelete(task)} aria-label="Excluir" title="Excluir"><Trash2 size={16} /></button>
      </div>
    </article>
  );
}
