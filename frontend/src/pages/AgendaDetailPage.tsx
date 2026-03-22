import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { ArrowLeft, Timer, Vote, BarChart3, RefreshCw, Calendar } from 'lucide-react';
import toast from 'react-hot-toast';
import { agendaApi } from '../services/api';
import type { Agenda } from '../types';
import StatusBadge from '../components/StatusBadge';
import CountdownTimer from '../components/CountdownTimer';
import ResultBar from '../components/ResultBar';
import OpenSessionModal from '../components/OpenSessionModal';
import VoteModal from '../components/VoteModal';

export default function AgendaDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [agenda, setAgenda]           = useState<Agenda | null>(null);
  const [loading, setLoading]         = useState(true);
  const [showSession, setShowSession] = useState(false);
  const [showVote, setShowVote]       = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await agendaApi.get(Number(id));
      setAgenda(data);
    } catch (e: any) {
      toast.error(e.message);
      navigate('/');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => {
    if (agenda?.status === 'OPEN') {
      const interval = setInterval(load, 15_000);
      return () => clearInterval(interval);
    }
  }, [agenda?.status, load]);

  if (loading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        {[180, 120, 200].map((h, i) => (
          <div key={i} style={{
            height: h, borderRadius: 'var(--radius)',
            background: 'linear-gradient(90deg, var(--bg-card) 25%, var(--bg-raised) 50%, var(--bg-card) 75%)',
            backgroundSize: '200% 100%', animation: 'shimmer 1.4s ease infinite',
          }} />
        ))}
        <style>{`@keyframes shimmer { 0%{background-position:-200% center} 100%{background-position:200% center} }`}</style>
      </div>
    );
  }

  if (!agenda) return null;

  const canOpenSession = agenda.status === 'CREATED';
  const canVote        = agenda.status === 'OPEN' && agenda.session?.open;
  const hasResult      = !!agenda.result;

  const card = (children: React.ReactNode, extra?: React.CSSProperties) => (
    <div style={{
      background: 'var(--bg-card)', border: '1px solid var(--border)',
      borderRadius: 'var(--radius)', padding: 24, ...extra,
    }}>
      {children}
    </div>
  );

  const sectionTitle = (icon: React.ReactNode, title: string) => (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 18 }}>
      <span style={{ color: 'var(--accent)' }}>{icon}</span>
      <h2 style={{ fontFamily: 'Syne', fontSize: 15, fontWeight: 700 }}>{title}</h2>
    </div>
  );

  return (
    <>
      {/* Back */}
      <button onClick={() => navigate('/')} style={{
        display: 'flex', alignItems: 'center', gap: 6,
        background: 'none', border: 'none', color: 'var(--text-2)',
        fontSize: 14, marginBottom: 24, padding: 0, cursor: 'pointer',
        transition: 'color 0.2s',
      }}
        onMouseEnter={e => (e.currentTarget.style.color = 'var(--text-1)')}
        onMouseLeave={e => (e.currentTarget.style.color = 'var(--text-2)')}>
        <ArrowLeft size={16} /> Voltar para Pautas
      </button>

      {/* Header card */}
      {card(
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12, marginBottom: 12 }}>
            <div>
              <h1 style={{ fontFamily: 'Syne', fontSize: 'clamp(20px,3vw,28px)', fontWeight: 800, letterSpacing: '-0.3px', marginBottom: 10 }}>
                {agenda.title}
              </h1>
              <StatusBadge status={agenda.status} />
            </div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              <button onClick={() => { setLoading(true); load(); }} style={{
                padding: '8px 14px', borderRadius: 'var(--radius-sm)',
                background: 'transparent', border: '1px solid var(--border)',
                color: 'var(--text-2)', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6,
                cursor: 'pointer', transition: 'all 0.2s',
              }}>
                <RefreshCw size={13} /> Atualizar
              </button>
              {canOpenSession && (
                <button onClick={() => setShowSession(true)} style={{
                  padding: '8px 18px', borderRadius: 'var(--radius-sm)',
                  background: 'var(--yes)', border: 'none', color: 'white',
                  fontSize: 13, fontWeight: 500, display: 'flex', alignItems: 'center', gap: 7,
                  cursor: 'pointer', transition: 'all 0.2s',
                }}>
                  <Timer size={14} /> Abrir Sessão
                </button>
              )}
              {canVote && (
                <button onClick={() => setShowVote(true)} style={{
                  padding: '8px 18px', borderRadius: 'var(--radius-sm)',
                  background: 'var(--accent)', border: 'none', color: 'white',
                  fontSize: 13, fontWeight: 500, display: 'flex', alignItems: 'center', gap: 7,
                  cursor: 'pointer', boxShadow: '0 0 20px var(--accent-glow)',
                }}>
                  <Vote size={14} /> Votar
                </button>
              )}
            </div>
          </div>

          {agenda.description && (
            <p style={{ fontSize: 14, color: 'var(--text-2)', marginTop: 14, lineHeight: 1.7 }}>
              {agenda.description}
            </p>
          )}

          <div style={{ display: 'flex', gap: 20, marginTop: 18, flexWrap: 'wrap' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, color: 'var(--text-3)' }}>
              <Calendar size={13} />
              Criada em {format(new Date(agenda.createdAt), "dd 'de' MMMM 'de' yyyy", { locale: ptBR })}
            </span>
            {agenda.session?.open && (
              <CountdownTimer endTime={agenda.session.endTime} onExpire={load} />
            )}
          </div>
        </>
      )}

      {/* Session details */}
      {agenda.session && card(
        <>
          {sectionTitle(<Timer size={16} />, 'Sessão de Votação')}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 }}>
            {[
              { label: 'Início', value: format(new Date(agenda.session.startTime), "dd/MM/yyyy 'às' HH:mm:ss") },
              { label: 'Encerramento', value: format(new Date(agenda.session.endTime), "dd/MM/yyyy 'às' HH:mm:ss") },
              { label: 'Status', value: agenda.session.open ? '🟢 Em andamento' : '🔴 Encerrada' },
            ].map(item => (
              <div key={item.label} style={{
                padding: '12px 16px', background: 'var(--bg-deep)',
                borderRadius: 10, border: '1px solid var(--border)',
              }}>
                <div style={{ fontSize: 11, color: 'var(--text-3)', marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  {item.label}
                </div>
                <div style={{ fontSize: 14, color: 'var(--text-1)', fontWeight: 500 }}>{item.value}</div>
              </div>
            ))}
          </div>
        </>,
        { marginTop: 16 }
      )}

      {/* Results */}
      {hasResult && agenda.result && card(
        <>
          {sectionTitle(<BarChart3 size={16} />, 'Resultado da Votação')}
          <ResultBar result={agenda.result} />

          {/* Stats grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10, marginTop: 18 }}>
            {[
              { label: 'Total de Votos', value: agenda.result.totalVotes, color: 'var(--text-1)' },
              { label: 'Votos Sim',      value: agenda.result.yesVotes,   color: 'var(--yes)' },
              { label: 'Votos Não',      value: agenda.result.noVotes,    color: 'var(--no)' },
            ].map(s => (
              <div key={s.label} style={{
                padding: '16px', background: 'var(--bg-deep)',
                borderRadius: 10, border: '1px solid var(--border)', textAlign: 'center',
              }}>
                <div style={{ fontFamily: 'Syne', fontSize: 28, fontWeight: 800, color: s.color }}>{s.value}</div>
                <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 4 }}>{s.label}</div>
              </div>
            ))}
          </div>

          {/* Winner banner */}
          {!agenda.result.sessionOpen && agenda.result.winner !== 'NO_VOTES' && (
            <div style={{
              marginTop: 18, padding: '16px 20px', borderRadius: 10,
              background: agenda.result.winner === 'YES' ? 'var(--yes-dim)' :
                          agenda.result.winner === 'NO'  ? 'var(--no-dim)'  : 'rgba(245,158,11,0.1)',
              border: `1px solid ${agenda.result.winner === 'YES' ? 'var(--yes)' :
                                   agenda.result.winner === 'NO'  ? 'var(--no)'  : '#f59e0b'}30`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontFamily: 'Syne', fontWeight: 700, fontSize: 17,
              color: agenda.result.winner === 'YES' ? 'var(--yes)' :
                     agenda.result.winner === 'NO'  ? 'var(--no)'  : '#f59e0b',
              gap: 10,
            }}>
              {agenda.result.winner === 'YES' && '✅ Pauta Aprovada'}
              {agenda.result.winner === 'NO'  && '❌ Pauta Reprovada'}
              {agenda.result.winner === 'TIE' && '🤝 Empate'}
            </div>
          )}
        </>,
        { marginTop: 16 }
      )}

      {showSession && (
        <OpenSessionModal
          agendaId={agenda.id} agendaTitle={agenda.title}
          onClose={() => setShowSession(false)} onOpened={load}
        />
      )}
      {showVote && (
        <VoteModal
          agendaId={agenda.id} agendaTitle={agenda.title}
          onClose={() => setShowVote(false)} onVoted={load}
        />
      )}
    </>
  );
}
