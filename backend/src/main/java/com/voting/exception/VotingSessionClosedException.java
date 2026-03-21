package com.voting.exception;

public class VotingSessionClosedException extends RuntimeException {
    public VotingSessionClosedException(Long agendaId) {
        super("Voting session is closed for agenda id: " + agendaId);
    }
}
