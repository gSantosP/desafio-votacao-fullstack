import axios from 'axios';
import type {
  Agenda, CreateAgendaRequest, OpenSessionRequest,
  VoteRequest, VoteResponse, VoteResult
} from '../types';

const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      'Erro inesperado. Tente novamente.';
    return Promise.reject(new Error(message));
  }
);

export const agendaApi = {
  list: () => api.get<Agenda[]>('/agendas').then((r) => r.data),
  get: (id: number) => api.get<Agenda>(`/agendas/${id}`).then((r) => r.data),
  create: (data: CreateAgendaRequest) =>
    api.post<Agenda>('/agendas', data).then((r) => r.data),
  openSession: (id: number, data: OpenSessionRequest) =>
    api.post<Agenda>(`/agendas/${id}/sessions`, data).then((r) => r.data),
  castVote: (id: number, data: VoteRequest) =>
    api.post<VoteResponse>(`/agendas/${id}/votes`, data).then((r) => r.data),
  getResults: (id: number) =>
    api.get<VoteResult>(`/agendas/${id}/results`).then((r) => r.data),
};
