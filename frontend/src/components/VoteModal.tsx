import { useState } from 'react';
import { X, ThumbsUp, ThumbsDown } from 'lucide-react';
import toast from 'react-hot-toast';
import { agendaApi } from '../services/api';
import type { VoteChoice } from '../types';

interface Props {
  agendaId: number;
  agendaTitle: string;
  onClose: () => void;
  onVoted: () => void;
}

export default function VoteModal({ agendaId, agendaTitle, onClose, onVoted }: Props) {
  const [cpf, setCpf] = useState('');
  const [choice, setChoice] = useState<VoteChoice | null>(null);
  const [loading, setLoading] = useState(false);

  const formatCpf = (v: string) => {
    const n = v.replace(/\D/g, '').slice(0, 11);
    return n.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
            .replace(/(\d{3})(\d{3})(\d{3})(\d{1,2})$/, '$1.$2.$3-$4')
            .replace(/(\d{3})(\d{3})(\d{1,3})$/, '$1.$2.$3')
            .replace(/(\d{3})(\d{1,3})$/, '$1.$2')
            .replace(/^(\d{1,3})$/, '$1');
  };

  const handleVote = async () => {
    const cleanCpf = cpf.replace(/\D/g, '');
    if (cleanCpf.length !== 11) { toast.error('CPF inválido'); return; }
    if (!choice) { toast.error('Selecione Sim ou Não'); return; }
    setLoading(true);
    try {
      await agendaApi.castVote(agendaId, { cpf: cleanCpf, choice });
      toast.success(`Voto "${choice === 'YES' ? 'Sim' : 'Não'}" registrado com sucesso!`);
      onVoted(); onClose();
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 200,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'rgba(0,0,0,0.65)', backdropFilter: 'blur(6px)',
      animation: 'fadeIn 0.2s ease',
    }} onClick={onClose}>
      <div style={{
        background: 'var(--bg-card)', border: '1px solid var(--border)',
        borderRadius: 'var(--radius)', width: '100%', maxWidth: 440,
        padding: 28, margin: 16, animation: 'fadeUp 0.25s ease',
        boxShadow: '0 24px 60px rgba(0,0,0,0.5)',
      }} onClick={e => e.stopPropagation()}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
          <h2 style={{ fontSize: 18, fontWeight: 700 }}>Registrar Voto</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-3)', lineHeight: 0, padding: 4 }}>
            <X size={18} />
          </button>
        </div>
        <p style={{ fontSize: 13, color: 'var(--text-2)', marginBottom: 24 }}>
          Pauta: <strong style={{ color: 'var(--text-1)' }}>{agendaTitle}</strong>
        </p>

        {/* CPF */}
        <label style={{ fontSize: 12, color: 'var(--text-2)', fontWeight: 500, display: 'block', marginBottom: 6 }}>
          CPF do Associado
        </label>
        <input
          value={cpf} onChange={e => setCpf(formatCpf(e.target.value))}
          placeholder="000.000.000-00"
          style={{
            width: '100%', padding: '10px 14px', marginBottom: 22,
            background: 'var(--bg-deep)', border: '1px solid var(--border)',
            borderRadius: 'var(--radius-sm)', color: 'var(--text-1)', fontSize: 15,
            letterSpacing: '0.05em',
          }}
          onFocus={e => (e.target.style.borderColor = 'var(--border-hi)')}
          onBlur={e => (e.target.style.borderColor = 'var(--border)')}
        />

        {/* Choice buttons */}
        <label style={{ fontSize: 12, color: 'var(--text-2)', fontWeight: 500, display: 'block', marginBottom: 10 }}>
          Seu Voto
        </label>
        <div style={{ display: 'flex', gap: 12, marginBottom: 26 }}>
          {(['YES', 'NO'] as VoteChoice[]).map(v => {
            const isYes = v === 'YES';
            const selected = choice === v;
            return (
              <button key={v} onClick={() => setChoice(v)} style={{
                flex: 1, padding: '18px 0', borderRadius: 'var(--radius)',
                border: `2px solid ${selected ? (isYes ? 'var(--yes)' : 'var(--no)') : 'var(--border)'}`,
                background: selected ? (isYes ? 'var(--yes-dim)' : 'var(--no-dim)') : 'var(--bg-deep)',
                color: selected ? (isYes ? 'var(--yes)' : 'var(--no)') : 'var(--text-2)',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8,
                transition: 'all 0.2s', fontFamily: 'Syne', fontWeight: 700, fontSize: 15,
                cursor: 'pointer',
              }}
                onMouseEnter={e => { if (!selected) e.currentTarget.style.borderColor = isYes ? 'var(--yes)' : 'var(--no)'; }}
                onMouseLeave={e => { if (!selected) e.currentTarget.style.borderColor = 'var(--border)'; }}
              >
                {isYes ? <ThumbsUp size={22} /> : <ThumbsDown size={22} />}
                {isYes ? 'Sim' : 'Não'}
              </button>
            );
          })}
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
          <button onClick={onClose} style={{
            padding: '9px 18px', borderRadius: 'var(--radius-sm)',
            background: 'transparent', border: '1px solid var(--border)',
            color: 'var(--text-2)', fontSize: 14,
          }}>Cancelar</button>
          <button onClick={handleVote} disabled={loading || !choice || cpf.replace(/\D/g,'').length !== 11}
            style={{
              padding: '9px 22px', borderRadius: 'var(--radius-sm)',
              background: !choice || loading ? 'var(--bg-raised)' : 'var(--accent)',
              border: 'none', color: 'white', fontSize: 14, fontWeight: 500,
              transition: 'all 0.2s', opacity: loading ? 0.7 : 1,
            }}>
            {loading ? 'Enviando...' : 'Confirmar Voto'}
          </button>
        </div>
      </div>
    </div>
  );
}
