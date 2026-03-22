import type { VoteResult } from '../types';

interface Props { result: VoteResult; }

export default function VoteResultBar({ result }: Props) {
  const { totalVotes, yesVotes, noVotes, winner } = result;
  const yesPct = totalVotes > 0 ? Math.round((yesVotes / totalVotes) * 100) : 0;
  const noPct  = totalVotes > 0 ? Math.round((noVotes  / totalVotes) * 100) : 0;

  const winnerLabel: Record<string, string> = {
    YES: '✅ SIM venceu', NO: '❌ NÃO venceu',
    TIE: '🤝 Empate', NO_VOTES: 'Sem votos ainda',
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      {/* Winner banner */}
      {totalVotes > 0 && (
        <div style={{
          textAlign: 'center', padding: '10px 16px',
          borderRadius: 'var(--radius-sm)',
          background: winner === 'YES' ? 'var(--yes-dim)' : winner === 'NO' ? 'var(--no-dim)' : 'rgba(139,92,246,0.1)',
          border: `1px solid ${winner === 'YES' ? 'rgba(16,185,129,0.3)' : winner === 'NO' ? 'rgba(239,68,68,0.3)' : 'var(--border)'}`,
          fontFamily: 'Syne', fontWeight: 700, fontSize: 15,
          color: winner === 'YES' ? 'var(--yes)' : winner === 'NO' ? 'var(--no)' : 'var(--text-2)',
        }}>
          {winnerLabel[winner] ?? winner}
        </div>
      )}

      {/* Stats row */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10, textAlign: 'center' }}>
        {[
          { label: 'SIM', value: yesVotes, pct: yesPct, color: 'var(--yes)' },
          { label: 'Total', value: totalVotes, pct: null, color: 'var(--text-2)' },
          { label: 'NÃO', value: noVotes,  pct: noPct,  color: 'var(--no)' },
        ].map(({ label, value, pct, color }) => (
          <div key={label} style={{
            padding: '12px 8px', borderRadius: 'var(--radius-sm)',
            background: 'var(--bg-deep)', border: '1px solid var(--border)',
          }}>
            <div style={{ fontSize: 22, fontWeight: 700, fontFamily: 'Syne', color }}>{value}</div>
            <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>
              {label}{pct !== null ? ` (${pct}%)` : ''}
            </div>
          </div>
        ))}
      </div>

      {/* Progress bar */}
      {totalVotes > 0 && (
        <div>
          <div style={{
            height: 10, borderRadius: 5,
            background: 'var(--bg-deep)',
            overflow: 'hidden', display: 'flex',
            border: '1px solid var(--border)',
          }}>
            <div style={{
              width: `${yesPct}%`, background: 'var(--yes)',
              transition: 'width 0.6s ease',
            }} />
            <div style={{
              width: `${noPct}%`, background: 'var(--no)',
              transition: 'width 0.6s ease',
            }} />
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: 'var(--text-3)', marginTop: 4 }}>
            <span style={{ color: 'var(--yes)' }}>SIM {yesPct}%</span>
            <span style={{ color: 'var(--no)' }}>NÃO {noPct}%</span>
          </div>
        </div>
      )}

      {totalVotes === 0 && (
        <p style={{ textAlign: 'center', color: 'var(--text-3)', fontSize: 14 }}>
          Nenhum voto registrado ainda.
        </p>
      )}
    </div>
  );
}
