package com.voting.exception;

import com.voting.dto.VotingDTOs;
import com.voting.facade.CpfValidationFacade;
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
     * Handles invalid/rejected CPF from the external validation facade.
     * Returns 404 as per the Bonus Task 1 specification.
     */
    @ExceptionHandler(CpfValidationFacade.CpfNotFoundException.class)
    public ResponseEntity<VotingDTOs.ErrorResponse> handleCpfNotFound(CpfValidationFacade.CpfNotFoundException ex) {
        log.warn("CPF not found or invalid: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    /**
     * Handles race condition on duplicate votes.
     * Two concurrent requests from the same CPF may pass the existence check
     * simultaneously. The unique constraint on (agenda_id, associate_cpf)
     * catches this at DB level and this handler converts it to a clean 409.
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
