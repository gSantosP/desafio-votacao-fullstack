import { useState, useEffect, useCallback } from 'react';
import { Plus, RefreshCw, Vote, ListChecks, CheckCircle, Clock } from 'lucide-react';
import toast from 'react-hot-toast';
import { agendaApi } from '../services/api';
import type { Agenda } from '../types';
import AgendaCard from '../components/AgendaCard';
import CreateAgendaModal from '../components/CreateAgendaModal';

type Filter = 'ALL' | 'CREATED' | 'OPEN' | 'CLOSED';

const filterConfig: { key: Filter; label: string; icon: React.ReactNode }[] = [
  { key: 'ALL',     label: 'Todas',    icon: <ListChecks size={14} /> },
  { key: 'OPEN',    label: 'Abertas',  icon: <Clock size={14} /> },
  { key: 'CREATED', label: 'Criadas',  icon: <Vote size={14} /> },
  { key: 'CLOSED',  label: 'Fechadas', icon: <CheckCircle size={14} /> },
];

export default function HomePage() {
  const [agendas, setAgendas]       = useState<Agenda[]>([]);
  const [loading, setLoading]       = useState(true);
  const [filter, setFilter]         = useState<Filter>('ALL');
  const [showCreate, setShowCreate] = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await agendaApi.list();
      setAgendas(data);
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);
  // Auto-refresh every 30s to pick up closed sessions
  useEffect(() => { const id = setInterval(load, 30_000); return () => clearInterval(id); }, [load]);

  const filtered = filter === 'ALL' ? agendas : agendas.filter(a => a.status === filter);

  const counts: Record<Filter, number> = {
    ALL:     agendas.length,
    OPEN:    agendas.filter(a => a.status === 'OPEN').length,
    CREATED: agendas.filter(a => a.status === 'CREATED').length,
    CLOSED:  agendas.filter(a => a.status === 'CLOSED').length,
  };

  return (
    <>
      {/* Hero */}
      <div style={{ marginBottom: 36 }}>
        <h1 style={{ fontFamily: 'Syne', fontSize: 'clamp(26px,4vw,38px)', fontWeight: 800, letterSpacing: '-0.5px', marginBottom: 8 }}>
          Assembleias &amp; Votações
        </h1>
        <p style={{ color: 'var(--text-2)', fontSize: 15, maxWidth: 480 }}>
          Gerencie pautas cooperativas, abra sessões e registre votos de forma simples e transparente.
        </p>
      </div>

      {/* Toolbar */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 28, flexWrap: 'wrap', alignItems: 'center' }}>
        {/* Filters */}
        <div style={{ display: 'flex', gap: 6, background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 10, padding: 4 }}>
          {filterConfig.map(f => (
            <button key={f.key} onClick={() => setFilter(f.key)} style={{
              padding: '6px 14px', borderRadius: 7, fontSize: 13, fontWeight: 500,
              display: 'flex', alignItems: 'center', gap: 5,
              background: filter === f.key ? 'var(--accent)' : 'transparent',
              border: 'none', color: filter === f.key ? 'white' : 'var(--text-2)',
              transition: 'all 0.18s', cursor: 'pointer',
            }}>
              {f.icon}{f.label}
              <span style={{
                fontSize: 11, background: filter === f.key ? 'rgba(255,255,255,0.2)' : 'var(--bg-deep)',
                borderRadius: 99, padding: '1px 6px', color: filter === f.key ? 'white' : 'var(--text-3)',
              }}>{counts[f.key]}</span>
            </button>
          ))}
        </div>

        <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
          <button onClick={() => { setLoading(true); load(); }} style={{
            padding: '8px 14px', borderRadius: 'var(--radius-sm)',
            background: 'transparent', border: '1px solid var(--border)',
            color: 'var(--text-2)', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6,
            transition: 'all 0.2s', cursor: 'pointer',
          }}>
            <RefreshCw size={14} style={{ animation: loading ? 'spin 1s linear infinite' : 'none' }} />
            Atualizar
          </button>
          <button onClick={() => setShowCreate(true)} style={{
            padding: '8px 18px', borderRadius: 'var(--radius-sm)',
            background: 'var(--accent)', border: 'none', color: 'white',
            fontSize: 13, fontWeight: 500, display: 'flex', alignItems: 'center', gap: 7,
            transition: 'all 0.2s', cursor: 'pointer',
            boxShadow: '0 0 20px var(--accent-glow)',
          }}
            onMouseEnter={e => (e.currentTarget.style.background = 'var(--accent-dim)')}
            onMouseLeave={e => (e.currentTarget.style.background = 'var(--accent)')}>
            <Plus size={15} /> Nova Pauta
          </button>
        </div>
      </div>

      {/* Grid */}
      {loading ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 16 }}>
          {[...Array(3)].map((_, i) => (
            <div key={i} style={{
              height: 160, borderRadius: 'var(--radius)',
              background: 'linear-gradient(90deg, var(--bg-card) 25%, var(--bg-raised) 50%, var(--bg-card) 75%)',
              backgroundSize: '200% 100%', animation: 'shimmer 1.4s ease infinite',
            }} />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '64px 24px', color: 'var(--text-3)' }}>
          <Vote size={40} style={{ margin: '0 auto 14px', opacity: 0.4 }} />
          <p style={{ fontSize: 16, marginBottom: 6 }}>
            {filter === 'ALL' ? 'Nenhuma pauta criada ainda.' : `Nenhuma pauta "${filter.toLowerCase()}".`}
          </p>
          {filter === 'ALL' && (
            <button onClick={() => setShowCreate(true)} style={{
              marginTop: 12, padding: '8px 20px', borderRadius: 'var(--radius-sm)',
              background: 'var(--accent)', border: 'none', color: 'white', fontSize: 14, cursor: 'pointer',
            }}>
              Criar primeira pauta
            </button>
          )}
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 16 }}>
          {filtered.map((a, i) => (
            <div key={a.id} style={{ animationDelay: `${i * 0.05}s` }}>
              <AgendaCard agenda={a} onRefresh={load} />
            </div>
          ))}
        </div>
      )}

      {showCreate && <CreateAgendaModal onClose={() => setShowCreate(false)} onCreated={load} />}

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </>
  );
}
