import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowRight, FolderKanban, Plus } from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { Link } from 'react-router-dom';
import { AppHeader } from '../components/AppHeader';
import { Modal } from '../components/Modal';
import { ProjectForm } from '../components/ProjectForm';
import { getErrorMessage } from '../lib/api';
import { projectService } from '../services/projectService';
import { useAuthStore } from '../store/authStore';

export function ProjectsPage() {
  const [showCreate, setShowCreate] = useState(false);
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const projects = useQuery({ queryKey: ['projects'], queryFn: () => projectService.list(0, 100) });
  const create = useMutation({
    mutationFn: projectService.create,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['projects'] }); setShowCreate(false); toast.success('Projeto criado.'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  return <div className="app-shell"><AppHeader /><main className="page-container">
    <div className="page-title-row"><div><span className="eyebrow">Workspace</span><h1>Seus projetos</h1><p>Projetos dos quais você é dono ou membro.</p></div>
      {user?.role === 'ADMIN' && <button className="button primary" onClick={() => setShowCreate(true)} data-testid="new-project"><Plus size={18} />Novo projeto</button>}
    </div>
    {projects.isLoading && <div className="loading-card">Carregando projetos...</div>}
    {projects.isError && <div className="error-card">{getErrorMessage(projects.error)}</div>}
    <section className="project-grid">
      {projects.data?.content.map((project) => <Link key={project.id} to={`/projects/${project.id}`} className="project-card" data-testid={`project-${project.id}`}>
        <div className="project-icon"><FolderKanban /></div><div className="project-card-main"><h2>{project.name}</h2><p>{project.description || 'Sem descrição.'}</p>
          <div className="project-card-meta"><span>{project.memberCount} membro(s)</span><span>Dono: {project.owner.name}</span></div></div><ArrowRight size={20} />
      </Link>)}
    </section>
    {projects.data?.content.length === 0 && <div className="empty-state"><FolderKanban size={42} /><h2>Nenhum projeto disponível</h2><p>Um ADMIN pode criar o primeiro projeto.</p></div>}
    {showCreate && <Modal title="Novo projeto" onClose={() => setShowCreate(false)}><ProjectForm onSubmit={(payload) => create.mutate(payload)} onCancel={() => setShowCreate(false)} submitting={create.isPending} /></Modal>}
  </main></div>;
}
