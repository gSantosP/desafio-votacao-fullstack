import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { server } from '../test/server';
import { http, HttpResponse } from 'msw';
import HomePage from '../pages/HomePage';

beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

const renderHomePage = () =>
  render(
    <MemoryRouter>
      <Toaster />
      <HomePage />
    </MemoryRouter>
  );

describe('HomePage', () => {
  it('renders page title', () => {
    renderHomePage();
    expect(screen.getByText('Assembleias & Votações')).toBeInTheDocument();
  });

  it('loads and displays agendas from API', async () => {
    renderHomePage();
    await waitFor(() => {
      expect(screen.getByText('Reforma do Estatuto')).toBeInTheDocument();
      expect(screen.getByText('Eleição da Diretoria')).toBeInTheDocument();
      expect(screen.getByText('Aprovação do Orçamento')).toBeInTheDocument();
    });
  });

  it('shows correct status badges', async () => {
    renderHomePage();
    await waitFor(() => {
      expect(screen.getByText('Aberta')).toBeInTheDocument();
      expect(screen.getByText('Fechada')).toBeInTheDocument();
      expect(screen.getByText('Criada')).toBeInTheDocument();
    });
  });

  it('filters agendas by OPEN status', async () => {
    const user = userEvent.setup();
    renderHomePage();
    await waitFor(() => screen.getByText('Reforma do Estatuto'));
    await user.click(screen.getByText('Abertas'));
    await waitFor(() => {
      expect(screen.getByText('Reforma do Estatuto')).toBeInTheDocument();
      expect(screen.queryByText('Eleição da Diretoria')).not.toBeInTheDocument();
    });
  });

  it('filters agendas by CLOSED status', async () => {
    const user = userEvent.setup();
    renderHomePage();
    await waitFor(() => screen.getByText('Eleição da Diretoria'));
    await user.click(screen.getByText('Fechadas'));
    await waitFor(() => {
      expect(screen.getByText('Eleição da Diretoria')).toBeInTheDocument();
      expect(screen.queryByText('Reforma do Estatuto')).not.toBeInTheDocument();
    });
  });

  it('shows empty state when no agendas', async () => {
    server.use(
      http.get('/api/v1/agendas', () => HttpResponse.json([]))
    );
    renderHomePage();
    await waitFor(() => {
      expect(screen.getByText('Nenhuma pauta criada ainda.')).toBeInTheDocument();
    });
  });
});
