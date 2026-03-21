package com.voting.exception;

public class SessionAlreadyOpenException extends RuntimeException {
    public SessionAlreadyOpenException(Long agendaId) {
        super("A voting session already exists for agenda id: " + agendaId);
    }
}
