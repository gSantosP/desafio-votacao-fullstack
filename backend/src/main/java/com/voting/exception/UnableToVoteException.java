package com.voting.exception;

public class UnableToVoteException extends RuntimeException {
    public UnableToVoteException(String cpf) {
        super("Associate with CPF " + cpf + " is not eligible to vote");
    }
}
