import { useDroppable } from '@dnd-kit/core';
import type { Task, TaskStatus } from '../types/api';
import { TaskCard } from './TaskCard';

const labels: Record<TaskStatus, string> = { TODO: 'A fazer', IN_PROGRESS: 'Em andamento', DONE: 'Concluídas' };

interface TaskColumnProps {
  status: TaskStatus;
  tasks: Task[];
  onEdit: (task: Task) => void;
  onDelete: (task: Task) => void;
  onHistory: (task: Task) => void;
}

export function TaskColumn({ status, tasks, onEdit, onDelete, onHistory }: TaskColumnProps) {
  const { setNodeRef, isOver } = useDroppable({ id: status });
  return (
    <section ref={setNodeRef} className={`task-column ${isOver ? 'column-over' : ''}`} data-testid={`column-${status}`}>
      <header><h2>{labels[status]}</h2><span>{tasks.length}</span></header>
      <div className="task-list">
        {tasks.map((task) => <TaskCard key={task.id} task={task} onEdit={onEdit} onDelete={onDelete} onHistory={onHistory} />)}
        {tasks.length === 0 && <div className="empty-column">Solte uma tarefa aqui</div>}
      </div>
    </section>
  );
}
