package com.voting.exception;

public class DuplicateVoteException extends RuntimeException {
    public DuplicateVoteException(String cpf, Long agendaId) {
        super("Associate with CPF " + cpf + " has already voted on agenda id: " + agendaId);
    }
}
