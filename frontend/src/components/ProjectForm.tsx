import { useState, type FormEvent } from 'react';

interface ProjectFormProps {
  initial?: { name: string; description?: string | null };
  submitting?: boolean;
  onSubmit: (payload: { name: string; description?: string }) => void;
  onCancel: () => void;
}

export function ProjectForm({ initial, submitting, onSubmit, onCancel }: ProjectFormProps) {
  const [name, setName] = useState(initial?.name ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');

  const submit = (event: FormEvent) => {
    event.preventDefault();
    onSubmit({ name: name.trim(), description: description.trim() || undefined });
  };

  return (
    <form className="stack-form" onSubmit={submit} data-testid="project-form">
      <label>
        Nome
        <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={120} data-testid="project-name" />
      </label>
      <label>
        Descrição
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} maxLength={1000} rows={4} />
      </label>
      <div className="form-actions">
        <button type="button" className="button secondary" onClick={onCancel}>Cancelar</button>
        <button type="submit" className="button primary" disabled={submitting || !name.trim()} data-testid="save-project">
          {submitting ? 'Salvando...' : 'Salvar projeto'}
        </button>
      </div>
    </form>
  );
}
