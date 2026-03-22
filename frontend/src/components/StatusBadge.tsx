import type { AgendaStatus } from '../types';

const config: Record<AgendaStatus, { label: string; color: string; bg: string; dot: string }> = {
  CREATED: { label: 'Criada',  color: '#a89ec8', bg: 'rgba(168,158,200,0.1)', dot: '#a89ec8' },
  OPEN:    { label: 'Aberta',  color: '#10b981', bg: 'rgba(16,185,129,0.12)', dot: '#10b981' },
  CLOSED:  { label: 'Fechada', color: '#6b6490', bg: 'rgba(107,100,144,0.1)', dot: '#6b6490' },
};

export default function StatusBadge({ status }: { status: AgendaStatus }) {
  const c = config[status];
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      padding: '3px 10px', borderRadius: 20,
      fontSize: 12, fontWeight: 500,
      color: c.color, background: c.bg,
      border: `1px solid ${c.color}30`,
    }}>
      <span style={{
        width: 6, height: 6, borderRadius: '50%',
        background: c.dot,
        ...(status === 'OPEN' ? { animation: 'pulse-ring 1.5s ease infinite' } : {}),
      }} />
      {c.label}
    </span>
  );
}
