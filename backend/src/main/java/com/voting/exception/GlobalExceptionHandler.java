package com.voting.exception;

import com.voting.dto.VotingDTOs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AgendaNotFoundException.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleAgendaNotFound(AgendaNotFoundException ex) {
        log.warn("Agenda not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(SessionAlreadyOpenException.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleSessionAlreadyOpen(SessionAlreadyOpenException ex) {
        log.warn("Session already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(VotingSessionClosedException.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleSessionClosed(VotingSessionClosedException ex) {
        log.warn("Session closed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(buildError(HttpStatus.UNPROCESSABLE_ENTITY, "Session Closed", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateVoteException.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleDuplicateVote(DuplicateVoteException ex) {
        log.warn("Duplicate vote: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, "Duplicate Vote", ex.getMessage()));
    }

    @ExceptionHandler(UnableToVoteException.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleUnableToVote(UnableToVoteException ex) {
        log.warn("Unable to vote: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()));
    }

    /**
     * Handles race condition on duplicate votes.
     *
     * Scenario: two concurrent requests from the same CPF pass the
     * existsByAgendaIdAndAssociateCpf() check simultaneously before either
     * is persisted. The second INSERT hits the unique constraint
     * (agenda_id, associate_cpf) and throws DataIntegrityViolationException.
     *
     * The @UniqueConstraint on the Vote entity is the last line of defense.
     * This handler converts the DB error into a meaningful 409 response
     * instead of leaking a 500 Internal Server Error to the client.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation — likely duplicate vote under concurrency: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, "Duplicate Vote",
                        "This CPF has already voted on this agenda"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Validation Error", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                        "An unexpected error occurred"));
    }

    private VotingDTOs.ErrorResponse buildError(HttpStatus status, String error, String message) {
        return VotingDTOs.ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
