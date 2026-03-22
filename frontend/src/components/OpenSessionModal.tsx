import { useState } from 'react';
import { X, Timer } from 'lucide-react';
import toast from 'react-hot-toast';
import { agendaApi } from '../services/api';

interface Props {
  agendaId: number;
  agendaTitle: string;
  onClose: () => void;
  onOpened: () => void;
}

export default function OpenSessionModal({ agendaId, agendaTitle, onClose, onOpened }: Props) {
  const [duration, setDuration] = useState('');
  const [loading, setLoading] = useState(false);

  const handleOpen = async () => {
    setLoading(true);
    try {
      const mins = duration ? parseInt(duration, 10) : undefined;
      if (mins !== undefined && (isNaN(mins) || mins < 1)) {
        toast.error('Duração deve ser pelo menos 1 minuto'); setLoading(false); return;
      }
      await agendaApi.openSession(agendaId, { durationMinutes: mins });
      toast.success(`Sessão aberta por ${mins ?? 1} minuto(s)!`);
      onOpened(); onClose();
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  };

  const presets = [1, 5, 10, 30, 60];

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 200,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(6px)',
      animation: 'fadeIn 0.2s ease',
    }} onClick={onClose}>
      <div style={{
        background: 'var(--bg-card)', border: '1px solid var(--border)',
        borderRadius: 'var(--radius)', width: '100%', maxWidth: 440,
        padding: 28, margin: 16, animation: 'fadeUp 0.25s ease',
        boxShadow: '0 24px 60px rgba(0,0,0,0.5)',
      }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
          <h2 style={{ fontSize: 18, fontWeight: 700 }}>Abrir Sessão de Votação</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-3)', lineHeight: 0, padding: 4 }}>
            <X size={18} />
          </button>
        </div>

        <p style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 22 }}>
          Pauta: <strong style={{ color: 'var(--text-1)' }}>{agendaTitle}</strong>
        </p>

        <label style={{ fontSize: 12, color: 'var(--text-2)', fontWeight: 500, display: 'block', marginBottom: 10 }}>
          Duração da Sessão
        </label>

        {/* Preset buttons */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 14, flexWrap: 'wrap' }}>
          {presets.map(p => (
            <button key={p}
              onClick={() => setDuration(String(p))}
              style={{
                padding: '6px 14px', borderRadius: 99, fontSize: 13,
                background: duration === String(p) ? 'var(--accent)' : 'var(--bg-deep)',
                border: `1px solid ${duration === String(p) ? 'var(--accent)' : 'var(--border)'}`,
                color: duration === String(p) ? 'white' : 'var(--text-2)',
                transition: 'all 0.18s', cursor: 'pointer',
              }}>
              {p}min
            </button>
          ))}
        </div>

        {/* Custom input */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 22 }}>
          <input
            type="number" min={1} max={1440} value={duration}
            onChange={e => setDuration(e.target.value)}
            placeholder="Outro (min) — padrão: 1"
            style={{
              flex: 1, padding: '10px 14px',
              background: 'var(--bg-deep)', border: '1px solid var(--border)',
              borderRadius: 'var(--radius-sm)', color: 'var(--text-1)', fontSize: 14,
            }}
            onFocus={e => (e.target.style.borderColor = 'var(--border-hi)')}
            onBlur={e => (e.target.style.borderColor = 'var(--border)')}
          />
          <Timer size={16} color="var(--text-3)" />
        </div>

        <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
          <button onClick={onClose} style={{
            padding: '9px 18px', borderRadius: 'var(--radius-sm)',
            background: 'transparent', border: '1px solid var(--border)',
            color: 'var(--text-2)', fontSize: 14,
          }}>Cancelar</button>
          <button onClick={handleOpen} disabled={loading} style={{
            padding: '9px 20px', borderRadius: 'var(--radius-sm)',
            background: loading ? 'var(--bg-raised)' : 'var(--yes)',
            border: 'none', color: 'white', fontSize: 14, fontWeight: 500,
            display: 'flex', alignItems: 'center', gap: 7,
            opacity: loading ? 0.7 : 1, transition: 'all 0.2s',
          }}>
            {loading ? 'Abrindo...' : <><Timer size={15} /> Abrir Sessão</>}
          </button>
        </div>
      </div>
    </div>
  );
}
