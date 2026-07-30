import { DndContext, PointerSensor, useSensor, useSensors, type DragEndEvent } from '@dnd-kit/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Plus, Settings, UserPlus, Users } from 'lucide-react';
import { useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import { Link, useParams } from 'react-router-dom';
import { AppHeader } from '../components/AppHeader';
import { Modal } from '../components/Modal';
import { SummaryPanel } from '../components/SummaryPanel';
import { TaskColumn } from '../components/TaskColumn';
import { TaskFiltersBar } from '../components/TaskFiltersBar';
import { TaskForm } from '../components/TaskForm';
import { useAssignmentNotifications } from '../hooks/useAssignmentNotifications';
import { getErrorMessage } from '../lib/api';
import { formatTaskAuditAction, formatTaskAuditFieldName, formatTaskAuditValue } from '../lib/taskAudit';
import { projectService } from '../services/projectService';
import { taskService } from '../services/taskService';
import { useAuthStore } from '../store/authStore';
import type { Task, TaskFilters, TaskPayload, TaskStatus } from '../types/api';

const statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

export function ProjectBoardPage() {
  const { projectId = '' } = useParams();
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<TaskFilters>({ size: 100, sortBy: 'PRIORITY', direction: 'DESC' });
  const [taskModal, setTaskModal] = useState<{ open: boolean; task?: Task }>({ open: false });
  const [historyTask, setHistoryTask] = useState<Task | null>(null);
  const [membersOpen, setMembersOpen] = useState(false);
  const [memberEmail, setMemberEmail] = useState('member@taskmanager.local');
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 6 } }));

  const project = useQuery({ queryKey: ['project', projectId], queryFn: () => projectService.get(projectId), enabled: !!projectId });
  const tasks = useQuery({ queryKey: ['tasks', projectId, filters], queryFn: () => taskService.list(projectId, filters), enabled: !!projectId });
  const summary = useQuery({ queryKey: ['summary', projectId], queryFn: () => taskService.summary(projectId), enabled: !!projectId });
  const history = useQuery({ queryKey: ['history', projectId, historyTask?.id], queryFn: () => taskService.history(projectId, historyTask!.id), enabled: !!historyTask });
  const taskList = useMemo(() => tasks.data?.content ?? [], [tasks.data?.content]
  );
  useAssignmentNotifications(taskList);

  const invalidateTaskData = () => {
    queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
    queryClient.invalidateQueries({ queryKey: ['summary', projectId] });
  };

  const createTask = useMutation({ mutationFn: (payload: TaskPayload) => taskService.create(projectId, payload), onSuccess: (task) => { invalidateTaskData(); setTaskModal({ open: false }); toast.success(task.assignee.id === user?.id ? 'Tarefa criada e atribuída a você.' : 'Tarefa criada.'); }, onError: (e) => toast.error(getErrorMessage(e)) });
  const updateTask = useMutation({ mutationFn: ({ id, payload }: { id: string; payload: TaskPayload }) => taskService.update(projectId, id, payload), onSuccess: () => { invalidateTaskData(); setTaskModal({ open: false }); toast.success('Tarefa atualizada.'); }, onError: (e) => toast.error(getErrorMessage(e)) });
  const changeStatus = useMutation({ mutationFn: ({ id, status }: { id: string; status: TaskStatus }) => taskService.updateStatus(projectId, id, status), onSuccess: () => { invalidateTaskData(); toast.success('Status atualizado.'); }, onError: (e) => { invalidateTaskData(); toast.error(getErrorMessage(e)); } });
  const deleteTask = useMutation({ mutationFn: (id: string) => taskService.remove(projectId, id), onSuccess: () => { invalidateTaskData(); toast.success('Tarefa excluída.'); }, onError: (e) => toast.error(getErrorMessage(e)) });
  const addMember = useMutation({ mutationFn: () => projectService.addMember(projectId, memberEmail), onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['project', projectId] }); setMemberEmail(''); toast.success('Membro adicionado.'); }, onError: (e) => toast.error(getErrorMessage(e)) });
  const removeMember = useMutation({ mutationFn: (userId: string) => projectService.removeMember(projectId, userId), onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['project', projectId] }); toast.success('Membro removido.'); }, onError: (e) => toast.error(getErrorMessage(e)) });

  const grouped = useMemo(() => Object.fromEntries(statuses.map((status) => [status, taskList.filter((t) => t.status === status)])) as Record<TaskStatus, Task[]>, [taskList]);
  const isOwner = project.data?.owner.id === user?.id;

  const onDragEnd = ({ active, over }: DragEndEvent) => {
    if (!over) return;
    const task = active.data.current?.task as Task | undefined;
    const target = over.id as TaskStatus;
    if (task && statuses.includes(target) && task.status !== target) changeStatus.mutate({ id: task.id, status: target });
  };

  if (project.isLoading) return <div className="app-shell"><AppHeader /><main className="page-container"><div className="loading-card">Carregando projeto...</div></main></div>;
  if (!project.data) return <div className="app-shell"><AppHeader /><main className="page-container"><div className="error-card">{getErrorMessage(project.error)}</div></main></div>;

  return <div className="app-shell"><AppHeader /><main className="board-page">
    <div className="board-header"><div><Link to="/projects" className="back-link"><ArrowLeft size={16} />Projetos</Link><h1>{project.data.name}</h1><p>{project.data.description || 'Sem descrição.'}</p></div>
      <div className="board-actions"><button className="button secondary" onClick={() => setMembersOpen(true)}><Users size={18} />Membros</button><button className="button primary" onClick={() => setTaskModal({ open: true })} data-testid="new-task"><Plus size={18} />Nova tarefa</button></div>
    </div>
    <SummaryPanel summary={summary.data} />
    <TaskFiltersBar filters={filters} members={project.data.members} onChange={setFilters} />
    {tasks.isError && <div className="error-card">{getErrorMessage(tasks.error)}</div>}
    <DndContext sensors={sensors} onDragEnd={onDragEnd}><div className="board-grid">{statuses.map((status) => <TaskColumn key={status} status={status} tasks={grouped[status]} onEdit={(task) => setTaskModal({ open: true, task })} onDelete={(task) => { if (confirm(`Excluir “${task.title}”?`)) deleteTask.mutate(task.id); }} onHistory={setHistoryTask} />)}</div></DndContext>
    {(tasks.data?.totalPages ?? 0) > 1 && <div className="pagination"><button disabled={tasks.data?.first} onClick={() => setFilters({ ...filters, page: Math.max(0, (filters.page ?? 0) - 1) })}>Anterior</button><span>Página {(tasks.data?.page ?? 0) + 1} de {tasks.data?.totalPages}</span><button disabled={tasks.data?.last} onClick={() => setFilters({ ...filters, page: (filters.page ?? 0) + 1 })}>Próxima</button></div>}

    {taskModal.open && <Modal title={taskModal.task ? 'Editar tarefa' : 'Nova tarefa'} onClose={() => setTaskModal({ open: false })}><TaskForm members={project.data.members} task={taskModal.task} submitting={createTask.isPending || updateTask.isPending} onCancel={() => setTaskModal({ open: false })} onSubmit={(payload) => taskModal.task ? updateTask.mutate({ id: taskModal.task.id, payload }) : createTask.mutate(payload)} /></Modal>}
    {historyTask && <Modal title={`Histórico — ${historyTask.title}`} onClose={() => setHistoryTask(null)} wide><div className="audit-list">{history.isLoading && <p>Carregando...</p>}{history.data?.content.map((item) => <div key={item.id} className="audit-item"><div><strong>{formatTaskAuditAction(item.action)}</strong><span>{formatTaskAuditFieldName(item.fieldName)}</span></div><p><del>{formatTaskAuditValue(item.fieldName, item.previousValue)}</del><span>→</span><ins>{formatTaskAuditValue(item.fieldName, item.newValue)}</ins></p><small>{item.changedBy.name} · {new Date(item.changedAt).toLocaleString('pt-BR')}</small></div>)}</div></Modal>}
    {membersOpen && <Modal title="Membros do projeto" onClose={() => setMembersOpen(false)}><div className="member-list">{project.data.members.map((member) => <div className="member-row" key={member.user.id}><div><strong>{member.user.name}</strong><small>{member.user.email} · {member.owner ? 'Dono' : member.user.role}</small></div>{isOwner && !member.owner && <button className="text-button danger" onClick={() => removeMember.mutate(member.user.id)}>Remover</button>}</div>)}</div>{isOwner && <form className="member-form" onSubmit={(e) => { e.preventDefault(); addMember.mutate(); }}><label>Email do usuário<input type="email" value={memberEmail} onChange={(e) => setMemberEmail(e.target.value)} required data-testid="member-email" /></label><button className="button primary" disabled={addMember.isPending} data-testid="add-member"><UserPlus size={17} />Adicionar</button></form>} {!isOwner && <p className="muted"><Settings size={15} /> Somente o dono gerencia membros.</p>}</Modal>}
  </main></div>;
}
