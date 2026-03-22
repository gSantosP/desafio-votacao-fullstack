import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { server } from '../test/server';
import { http, HttpResponse } from 'msw';
import { agendaApi } from '../services/api';

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('agendaApi', () => {
  describe('list()', () => {
    it('returns list of agendas', async () => {
      const agendas = await agendaApi.list();
      expect(agendas).toHaveLength(3);
      expect(agendas[0].title).toBe('Reforma do Estatuto');
    });

    it('throws on server error', async () => {
      server.use(
        http.get('/api/v1/agendas', () =>
          HttpResponse.json({ message: 'Server Error' }, { status: 500 })
        )
      );
      await expect(agendaApi.list()).rejects.toThrow();
    });
  });

  describe('create()', () => {
    it('creates agenda and returns it', async () => {
      const agenda = await agendaApi.create({ title: 'Nova Pauta', description: 'Desc' });
      expect(agenda.id).toBe(99);
      expect(agenda.title).toBe('Nova Pauta');
      expect(agenda.status).toBe('CREATED');
    });
  });

  describe('castVote()', () => {
    it('casts vote and returns vote response', async () => {
      const vote = await agendaApi.castVote(1, { cpf: '52998224725', choice: 'YES' });
      expect(vote.choice).toBe('YES');
      expect(vote.agendaId).toBe(1);
      expect(vote.maskedCpf).toBeTruthy();
    });

    it('throws on duplicate vote (409)', async () => {
      server.use(
        http.post('/api/v1/agendas/:id/votes', () =>
          HttpResponse.json({ message: 'Duplicate vote' }, { status: 409 })
        )
      );
      await expect(agendaApi.castVote(1, { cpf: '52998224725', choice: 'YES' }))
        .rejects.toThrow('Duplicate vote');
    });

    it('throws on invalid CPF (404)', async () => {
      server.use(
        http.post('/api/v1/agendas/:id/votes', () =>
          HttpResponse.json({ message: 'Invalid CPF' }, { status: 404 })
        )
      );
      await expect(agendaApi.castVote(1, { cpf: '00000000000', choice: 'YES' }))
        .rejects.toThrow('Invalid CPF');
    });
  });

  describe('getResults()', () => {
    it('returns vote results', async () => {
      const result = await agendaApi.getResults(1);
      expect(result.totalVotes).toBe(10);
      expect(result.yesVotes).toBe(7);
      expect(result.noVotes).toBe(3);
      expect(result.winner).toBe('YES');
    });
  });
});
