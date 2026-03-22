import { http, HttpResponse } from 'msw';
import type { Agenda } from '../../types';

export const mockAgendas: Agenda[] = [
  {
    id: 1,
    title: 'Reforma do Estatuto',
    description: 'Votação sobre alterações no estatuto social',
    status: 'OPEN',
    session: {
      id: 1,
      startTime: new Date().toISOString(),
      endTime: new Date(Date.now() + 5 * 60 * 1000).toISOString(),
      open: true,
      createdAt: new Date().toISOString(),
    },
    result: {
      agendaId: 1,
      agendaTitle: 'Reforma do Estatuto',
      agendaStatus: 'OPEN',
      totalVotes: 10,
      yesVotes: 7,
      noVotes: 3,
      winner: 'YES',
      sessionOpen: true,
    },
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 2,
    title: 'Eleição da Diretoria',
    description: null,
    status: 'CLOSED',
    session: {
      id: 2,
      startTime: new Date(Date.now() - 10 * 60 * 1000).toISOString(),
      endTime: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
      open: false,
      createdAt: new Date().toISOString(),
    },
    result: {
      agendaId: 2,
      agendaTitle: 'Eleição da Diretoria',
      agendaStatus: 'CLOSED',
      totalVotes: 20,
      yesVotes: 12,
      noVotes: 8,
      winner: 'YES',
      sessionOpen: false,
    },
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 3,
    title: 'Aprovação do Orçamento',
    description: 'Orçamento anual 2025',
    status: 'CREATED',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

export const handlers = [
  http.get('/api/v1/agendas', () => {
    return HttpResponse.json(mockAgendas);
  }),

  http.get('/api/v1/agendas/:id', ({ params }) => {
    const agenda = mockAgendas.find(a => a.id === Number(params.id));
    if (!agenda) return HttpResponse.json({ error: 'Not Found' }, { status: 404 });
    return HttpResponse.json(agenda);
  }),

  http.post('/api/v1/agendas', async ({ request }) => {
    const body = await request.json() as { title: string; description?: string };
    const newAgenda: Agenda = {
      id: 99,
      title: body.title,
      description: body.description,
      status: 'CREATED',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    return HttpResponse.json(newAgenda, { status: 201 });
  }),

  http.post('/api/v1/agendas/:id/sessions', ({ params }) => {
    const agenda = mockAgendas.find(a => a.id === Number(params.id));
    if (!agenda) return HttpResponse.json({ error: 'Not Found' }, { status: 404 });
    return HttpResponse.json({ ...agenda, status: 'OPEN' }, { status: 201 });
  }),

  http.post('/api/v1/agendas/:id/votes', async ({ request }) => {
    const body = await request.json() as { cpf: string; choice: string };
    return HttpResponse.json({
      id: 1,
      agendaId: 1,
      agendaTitle: 'Reforma do Estatuto',
      maskedCpf: '529.***.***-25',
      choice: body.choice,
      votedAt: new Date().toISOString(),
    }, { status: 201 });
  }),

  http.get('/api/v1/agendas/:id/results', ({ params }) => {
    const agenda = mockAgendas.find(a => a.id === Number(params.id));
    if (!agenda?.result) return HttpResponse.json({ error: 'Not Found' }, { status: 404 });
    return HttpResponse.json(agenda.result);
  }),
];
