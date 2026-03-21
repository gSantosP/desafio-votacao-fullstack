package com.voting.dto;

import com.voting.model.Agenda;
import com.voting.model.Vote;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class VotingDTOs {

    // ---- Agenda DTOs ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgendaRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgendaResponse {
        private Long id;
        private String title;
        private String description;
        private Agenda.AgendaStatus status;
        private SessionResponse session;
        private VoteResultResponse result;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ---- Session DTOs ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OpenSessionRequest {
        private Integer durationMinutes; // null = default 1 min
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionResponse {
        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean open;
        private LocalDateTime createdAt;
    }

    // ---- Vote DTOs ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VoteRequest {
        @NotBlank(message = "CPF is required")
        private String cpf;

        @NotNull(message = "Choice is required (YES or NO)")
        private Vote.VoteChoice choice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VoteResponse {
        private Long id;
        private Long agendaId;
        private String agendaTitle;
        private String maskedCpf;
        private Vote.VoteChoice choice;
        private LocalDateTime votedAt;
    }

    // ---- Result DTOs ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VoteResultResponse {
        private Long agendaId;
        private String agendaTitle;
        private Agenda.AgendaStatus agendaStatus;
        private long totalVotes;
        private long yesVotes;
        private long noVotes;
        private String winner;
        private boolean sessionOpen;
    }

    // ---- CPF Validation ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CpfValidationResponse {
        private String status; // ABLE_TO_VOTE or UNABLE_TO_VOTE
    }

    // ---- Error ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;
    }
}
