import { useState, useEffect } from 'react';
import { Clock } from 'lucide-react';
import { formatDistanceToNow, isPast } from 'date-fns';
import { ptBR } from 'date-fns/locale';

interface Props {
  endTime: string;
  onExpire?: () => void;
}

export default function CountdownTimer({ endTime, onExpire }: Props) {
  const end = new Date(endTime);
  const [timeLeft, setTimeLeft] = useState('');
  const [expired, setExpired] = useState(isPast(end));

  useEffect(() => {
    if (isPast(end)) {
      setExpired(true);
      onExpire?.();
      return;
    }

    const tick = () => {
      const now = new Date();
      if (now >= end) {
        setExpired(true);
        setTimeLeft('Encerrada');
        onExpire?.();
        clearInterval(id);
        return;
      }
      const diff = end.getTime() - now.getTime();
      const mins = Math.floor(diff / 60000);
      const secs = Math.floor((diff % 60000) / 1000);
      setTimeLeft(mins > 0 ? `${mins}m ${secs.toString().padStart(2, '0')}s` : `${secs}s`);
    };

    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [endTime]);

  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 5,
      fontSize: 13, fontWeight: 500,
      color: expired ? 'var(--text-3)' : '#f59e0b',
    }}>
      <Clock size={13} />
      {expired ? 'Encerrada' : `Fecha em ${timeLeft}`}
    </span>
  );
}
