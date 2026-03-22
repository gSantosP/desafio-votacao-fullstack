import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { ChevronRight, Users } from 'lucide-react';
import type { Agenda } from '../types';
import StatusBadge from './StatusBadge';
import CountdownTimer from './CountdownTimer';
import ResultBar from './ResultBar';

interface Props {
  agenda: Agenda;
  onRefresh: () => void;
}

export default function AgendaCard({ agenda, onRefresh }: Props) {
  const navigate = useNavigate();

  return (
    <div
      onClick={() => navigate(`/agendas/${agenda.id}`)}
      style={{
        background: 'var(--bg-card)', border: '1px solid var(--border)',
        borderRadius: 'var(--radius)', padding: 22, cursor: 'pointer',
        transition: 'all 0.22s ease',
        animation: 'fadeUp 0.35s ease both',
      }}
      onMouseEnter={e => {
        const el = e.currentTarget as HTMLDivElement;
        el.style.borderColor = 'var(--border-hi)';
        el.style.transform = 'translateY(-2px)';
        el.style.boxShadow = '0 8px 32px rgba(139,92,246,0.12)';
      }}
      onMouseLeave={e => {
        const el = e.currentTarget as HTMLDivElement;
        el.style.borderColor = 'var(--border)';
        el.style.transform = 'translateY(0)';
        el.style.boxShadow = 'none';
      }}
    >
      {/* Top row */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, marginBottom: 10 }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, flex: 1, minWidth: 0 }}>
          <h3 style={{
            fontFamily: 'Syne', fontWeight: 700, fontSize: 16,
            color: 'var(--text-1)', lineHeight: 1.3,
            overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
          }}>
            {agenda.title}
          </h3>
          <StatusBadge status={agenda.status} />
        </div>
        <ChevronRight size={16} color="var(--text-3)" style={{ flexShrink: 0, marginTop: 4 }} />
      </div>

      {/* Description */}
      {agenda.description && (
        <p style={{
          fontSize: 13, color: 'var(--text-2)', marginBottom: 14,
          overflow: 'hidden', display: '-webkit-box',
          WebkitLineClamp: 2, WebkitBoxOrient: 'vertical',
        }}>
          {agenda.description}
        </p>
      )}

      {/* Session countdown */}
      {agenda.session?.open && (
        <div style={{ marginBottom: 14 }}>
          <CountdownTimer endTime={agenda.session.endTime} onExpire={onRefresh} />
        </div>
      )}

      {/* Vote result bar */}
      {agenda.result && agenda.result.totalVotes > 0 && (
        <div style={{
          marginBottom: 14, padding: '12px 14px',
          background: 'var(--bg-deep)', borderRadius: 10, border: '1px solid var(--border)',
        }}>
          <ResultBar result={agenda.result} />
        </div>
      )}

      {/* Footer */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: 12, color: 'var(--text-3)' }}>
          {format(new Date(agenda.createdAt), "dd MMM yyyy 'às' HH:mm", { locale: ptBR })}
        </span>
        {agenda.result && (
          <span style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 12, color: 'var(--text-3)' }}>
            <Users size={12} /> {agenda.result.totalVotes} votos
          </span>
        )}
      </div>
    </div>
  );
}
