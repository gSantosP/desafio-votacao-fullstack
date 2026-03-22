import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import StatusBadge from '../components/StatusBadge';

describe('StatusBadge', () => {
  it('renders CREATED status correctly', () => {
    render(<StatusBadge status="CREATED" />);
    expect(screen.getByText('Criada')).toBeInTheDocument();
  });

  it('renders OPEN status correctly', () => {
    render(<StatusBadge status="OPEN" />);
    expect(screen.getByText('Aberta')).toBeInTheDocument();
  });

  it('renders CLOSED status correctly', () => {
    render(<StatusBadge status="CLOSED" />);
    expect(screen.getByText('Fechada')).toBeInTheDocument();
  });
});
