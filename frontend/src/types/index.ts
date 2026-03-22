export type AgendaStatus = 'CREATED' | 'OPEN' | 'CLOSED';
export type VoteChoice = 'YES' | 'NO';

export interface SessionResponse {
  id: number;
  startTime: string;
  endTime: string;
  open: boolean;
  createdAt: string;
}

export interface VoteResult {
  agendaId: number;
  agendaTitle: string;
  agendaStatus: AgendaStatus;
  totalVotes: number;
  yesVotes: number;
  noVotes: number;
  winner: 'YES' | 'NO' | 'TIE' | 'NO_VOTES';
  sessionOpen: boolean;
}

export interface Agenda {
  id: number;
  title: string;
  description?: string;
  status: AgendaStatus;
  session?: SessionResponse;
  result?: VoteResult;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAgendaRequest {
  title: string;
  description?: string;
}

export interface OpenSessionRequest {
  durationMinutes?: number;
}

export interface VoteRequest {
  cpf: string;
  choice: VoteChoice;
}

export interface VoteResponse {
  id: number;
  agendaId: number;
  agendaTitle: string;
  maskedCpf: string;
  choice: VoteChoice;
  votedAt: string;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}
