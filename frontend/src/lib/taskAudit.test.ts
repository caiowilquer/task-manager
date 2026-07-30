import { describe, expect, it } from 'vitest';
import { formatTaskAuditAction, formatTaskAuditFieldName, formatTaskAuditValue } from './taskAudit';

describe('task audit formatting', () => {
  it('renders Portuguese labels for audit actions', () => {
    expect(formatTaskAuditAction('CREATED')).toBe('Criado');
    expect(formatTaskAuditAction('UPDATED')).toBe('Atualizado');
    expect(formatTaskAuditAction('STATUS_CHANGED')).toBe('Status alterado');
    expect(formatTaskAuditAction('ASSIGNEE_CHANGED')).toBe('Responsável alterado');
  });

  it('renders Portuguese labels for audit fields and values', () => {
    expect(formatTaskAuditFieldName('priority')).toBe('Prioridade');
    expect(formatTaskAuditFieldName('status')).toBe('Status');
    expect(formatTaskAuditValue('status', 'TODO')).toBe('A fazer');
    expect(formatTaskAuditValue('status', 'IN_PROGRESS')).toBe('Em andamento');
    expect(formatTaskAuditValue('priority', 'HIGH')).toBe('Alta');
  });
});
