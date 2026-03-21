-- Performance indexes for high-volume voting scenarios
-- Applied automatically by Spring Boot on startup (schema.sql with ddl-auto: update)

-- Most critical: duplicate vote check on every castVote call
-- This query runs on EVERY vote: existsByAgendaIdAndAssociateCpf()
CREATE INDEX IF NOT EXISTS idx_votes_agenda_cpf
    ON votes (agenda_id, associate_cpf);

-- Vote counting queries (countYes / countNo / countTotal)
CREATE INDEX IF NOT EXISTS idx_votes_agenda_choice
    ON votes (agenda_id, choice);

-- Session lookup by agenda (every vote validates the session)
CREATE INDEX IF NOT EXISTS idx_sessions_agenda_id
    ON voting_sessions (agenda_id);

-- Finding expired sessions in the scheduled closer
CREATE INDEX IF NOT EXISTS idx_sessions_end_time
    ON voting_sessions (end_time);

-- Agenda listing ordered by creation date
CREATE INDEX IF NOT EXISTS idx_agendas_created_at
    ON agendas (created_at DESC);
