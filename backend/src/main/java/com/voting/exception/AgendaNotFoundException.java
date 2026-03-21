package com.voting.exception;

public class AgendaNotFoundException extends RuntimeException {
    public AgendaNotFoundException(Long id) {
        super("Agenda not found with id: " + id);
    }
}
