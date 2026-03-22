import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import CountdownTimer from '../components/CountdownTimer';

describe('CountdownTimer', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  it('renders time remaining when session is open', () => {
    const endTime = new Date(Date.now() + 90 * 1000).toISOString();
    render(<CountdownTimer endTime={endTime} />);
    expect(screen.getByText('Fecha em 1m 30s')).toBeInTheDocument();
  });

  it('renders "Encerrada" when session is expired', () => {
    const endTime = new Date(Date.now() - 1000).toISOString();
    render(<CountdownTimer endTime={endTime} />);
    expect(screen.getByText('Encerrada')).toBeInTheDocument();
  });

  it('calls onExpire when countdown reaches zero', () => {
    const onExpire = vi.fn();
    const endTime = new Date(Date.now() + 1000).toISOString();
    render(<CountdownTimer endTime={endTime} onExpire={onExpire} />);
    act(() => { vi.advanceTimersByTime(2000); });
    expect(onExpire).toHaveBeenCalledOnce();
  });

  it('counts down correctly', () => {
    const endTime = new Date(Date.now() + 61 * 1000).toISOString();
    render(<CountdownTimer endTime={endTime} />);
    expect(screen.getByText('Fecha em 1m 01s')).toBeInTheDocument();
    act(() => { vi.advanceTimersByTime(1000); });
    expect(screen.getByText('Fecha em 1m 00s')).toBeInTheDocument();
  });
});
