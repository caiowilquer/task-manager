import { Search, SlidersHorizontal } from 'lucide-react';
import type { ProjectMember, TaskFilters, TaskPriority, TaskStatus } from '../types/api';

interface Props {
  filters: TaskFilters;
  members: ProjectMember[];
  onChange: (filters: TaskFilters) => void;
}

export function TaskFiltersBar({ filters, members, onChange }: Props) {
  const set = (key: keyof TaskFilters, value: string) => onChange({ ...filters, [key]: value || undefined, page: 0 });
  return (
    <div className="filters-bar">
      <div className="search-box"><Search size={18} /><input placeholder="Buscar no título ou descrição" value={filters.query ?? ''} onChange={(e) => set('query', e.target.value)} /></div>
      <div className="filter-controls">
        <SlidersHorizontal size={18} />
        <select value={filters.status ?? ''} onChange={(e) => set('status', e.target.value as TaskStatus)} aria-label="Filtrar por status">
          <option value="">Todos os status</option><option value="TODO">A fazer</option><option value="IN_PROGRESS">Em andamento</option><option value="DONE">Concluída</option>
        </select>
        <select value={filters.priority ?? ''} onChange={(e) => set('priority', e.target.value as TaskPriority)} aria-label="Filtrar por prioridade">
          <option value="">Todas as prioridades</option><option value="LOW">Baixa</option><option value="MEDIUM">Média</option><option value="HIGH">Alta</option><option value="CRITICAL">Crítica</option>
        </select>
        <select value={filters.assigneeId ?? ''} onChange={(e) => set('assigneeId', e.target.value)} aria-label="Filtrar por responsável">
          <option value="">Todos os responsáveis</option>{members.map((m) => <option key={m.user.id} value={m.user.id}>{m.user.name}</option>)}
        </select>
        <select value={filters.sortBy ?? 'CREATED_AT'} onChange={(e) => set('sortBy', e.target.value)} aria-label="Ordenar por">
          <option value="CREATED_AT">Criação</option><option value="PRIORITY">Prioridade</option><option value="DEADLINE">Prazo</option>
        </select>
      </div>
    </div>
  );
}
