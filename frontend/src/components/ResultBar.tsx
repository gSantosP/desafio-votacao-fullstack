import type { VoteResult } from '../types';
import { ThumbsUp, ThumbsDown, Minus } from 'lucide-react';

export default function ResultBar({ result }: { result: VoteResult }) {
  const total = result.totalVotes;
  const yesPct = total > 0 ? Math.round((result.yesVotes / total) * 100) : 0;
  const noPct  = total > 0 ? Math.round((result.noVotes  / total) * 100) : 0;

  const winnerLabel: Record<string, string> = {
    YES: 'Aprovada ✓', NO: 'Reprovada ✗', TIE: 'Empate', NO_VOTES: 'Sem votos',
  };
  const winnerColor: Record<string, string> = {
    YES: 'var(--yes)', NO: 'var(--no)', TIE: '#f59e0b', NO_VOTES: 'var(--text-3)',
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
      {/* Totals row */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ display: 'flex', gap: 20 }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'var(--yes)', fontWeight: 600 }}>
            <ThumbsUp size={15} /> {result.yesVotes} Sim
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'var(--no)', fontWeight: 600 }}>
            <ThumbsDown size={15} /> {result.noVotes} Não
          </span>
          <span style={{ color: 'var(--text-3)', fontSize: 13 }}>
            {total} voto{total !== 1 ? 's' : ''}
          </span>
        </div>
        {!result.sessionOpen && (
          <span style={{
            fontFamily: 'Syne', fontWeight: 700, fontSize: 14,
            color: winnerColor[result.winner],
          }}>
            {winnerLabel[result.winner]}
          </span>
        )}
      </div>

      {/* Progress bar */}
      <div style={{ height: 10, borderRadius: 99, background: 'var(--bg-deep)', overflow: 'hidden', display: 'flex' }}>
        {yesPct > 0 && (
          <div style={{
            width: `${yesPct}%`, background: 'var(--yes)',
            transition: 'width 0.8s cubic-bezier(.4,0,.2,1)',
            borderRadius: noPct === 0 ? 99 : '99px 0 0 99px',
          }} />
        )}
        {noPct > 0 && (
          <div style={{
            width: `${noPct}%`, background: 'var(--no)',
            transition: 'width 0.8s cubic-bezier(.4,0,.2,1)',
            borderRadius: yesPct === 0 ? 99 : '0 99px 99px 0',
          }} />
        )}
      </div>

      {total > 0 && (
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-3)' }}>
          <span>{yesPct}%</span>
          <span>{noPct}%</span>
        </div>
      )}
    </div>
  );
}
