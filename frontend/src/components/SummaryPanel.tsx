import type { TaskSummary } from '../types/api';

export function SummaryPanel({ summary }: { summary?: TaskSummary }) {
  if (!summary) return null;
  return (
    <div className="summary-grid">
      <div className="summary-card"><span>A fazer</span><strong>{summary.byStatus.TODO}</strong></div>
      <div className="summary-card"><span>Em andamento</span><strong>{summary.byStatus.IN_PROGRESS}</strong></div>
      <div className="summary-card"><span>Concluídas</span><strong>{summary.byStatus.DONE}</strong></div>
      <div className="summary-card critical"><span>Críticas</span><strong>{summary.byPriority.CRITICAL}</strong></div>
    </div>
  );
}
