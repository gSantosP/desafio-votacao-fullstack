import { useState } from 'react';
import { X, Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { agendaApi } from '../services/api';

interface Props {
  onClose: () => void;
  onCreated: () => void;
}

export default function CreateAgendaModal({ onClose, onCreated }: Props) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!title.trim()) { toast.error('Título é obrigatório'); return; }
    setLoading(true);
    try {
      await agendaApi.create({ title: title.trim(), description: description.trim() || undefined });
      toast.success('Pauta criada com sucesso!');
      onCreated();
      onClose();
    } catch (e: any) {
      toast.error(e.message);
    } finally {
      setLoading(false);
    }
  };

  const inputStyle = {
    width: '100%', padding: '10px 14px',
    background: 'var(--bg-deep)', border: '1px solid var(--border)',
    borderRadius: 'var(--radius-sm)', color: 'var(--text-1)', fontSize: 14,
    transition: 'border-color 0.2s',
  } as const;

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 200,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(6px)',
      animation: 'fadeIn 0.2s ease',
    }} onClick={onClose}>
      <div style={{
        background: 'var(--bg-card)', border: '1px solid var(--border)',
        borderRadius: 'var(--radius)', width: '100%', maxWidth: 480,
        padding: 28, margin: 16,
        animation: 'fadeUp 0.25s ease',
        boxShadow: '0 24px 60px rgba(0,0,0,0.5)',
      }} onClick={e => e.stopPropagation()}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 22 }}>
          <h2 style={{ fontSize: 18, fontWeight: 700 }}>Nova Pauta</h2>
          <button onClick={onClose} style={{
            background: 'none', border: 'none', color: 'var(--text-3)',
            padding: 4, borderRadius: 6, lineHeight: 0,
            transition: 'color 0.2s',
          }}
            onMouseEnter={e => (e.currentTarget.style.color = 'var(--text-1)')}
            onMouseLeave={e => (e.currentTarget.style.color = 'var(--text-3)')}>
            <X size={18} />
          </button>
        </div>

        {/* Fields */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <div>
            <label style={{ fontSize: 12, color: 'var(--text-2)', fontWeight: 500, display: 'block', marginBottom: 6 }}>
              Título *
            </label>
            <input
              value={title}
              onChange={e => setTitle(e.target.value)}
              placeholder="Ex: Aprovação do orçamento anual"
              style={inputStyle}
              onFocus={e => (e.target.style.borderColor = 'var(--border-hi)')}
              onBlur={e => (e.target.style.borderColor = 'var(--border)')}
              onKeyDown={e => e.key === 'Enter' && handleSubmit()}
              maxLength={200}
              autoFocus
            />
          </div>
          <div>
            <label style={{ fontSize: 12, color: 'var(--text-2)', fontWeight: 500, display: 'block', marginBottom: 6 }}>
              Descrição (opcional)
            </label>
            <textarea
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Descreva os detalhes da pauta..."
              rows={3}
              style={{ ...inputStyle, resize: 'vertical', minHeight: 80 }}
              onFocus={e => (e.target.style.borderColor = 'var(--border-hi)')}
              onBlur={e => (e.target.style.borderColor = 'var(--border)')}
              maxLength={1000}
            />
          </div>
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', gap: 10, marginTop: 22, justifyContent: 'flex-end' }}>
          <button onClick={onClose} disabled={loading} style={{
            padding: '9px 18px', borderRadius: 'var(--radius-sm)',
            background: 'transparent', border: '1px solid var(--border)',
            color: 'var(--text-2)', fontSize: 14,
            transition: 'all 0.2s',
          }}
            onMouseEnter={e => { (e.currentTarget.style.borderColor = 'var(--border-hi)'); (e.currentTarget.style.color = 'var(--text-1)'); }}
            onMouseLeave={e => { (e.currentTarget.style.borderColor = 'var(--border)'); (e.currentTarget.style.color = 'var(--text-2)'); }}>
            Cancelar
          </button>
          <button onClick={handleSubmit} disabled={loading || !title.trim()} style={{
            padding: '9px 20px', borderRadius: 'var(--radius-sm)',
            background: loading || !title.trim() ? 'var(--bg-raised)' : 'var(--accent)',
            border: 'none', color: 'white', fontSize: 14, fontWeight: 500,
            display: 'flex', alignItems: 'center', gap: 7,
            transition: 'all 0.2s', opacity: loading ? 0.7 : 1,
          }}>
            {loading ? 'Criando...' : <><Plus size={15} /> Criar Pauta</>}
          </button>
        </div>
      </div>
    </div>
  );
}
