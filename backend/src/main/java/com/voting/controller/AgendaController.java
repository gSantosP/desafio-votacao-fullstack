package com.voting.controller;

import com.voting.dto.VotingDTOs;
import com.voting.service.AgendaService;
import com.voting.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Agenda management - API Version 1.
 * Versioning strategy: URI path versioning (/api/v1/...) chosen for
 * simplicity, visibility, and easy routing/caching at infrastructure level.
 */
@RestController
@RequestMapping("/api/v1/agendas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agendas", description = "Agenda (Pauta) management endpoints")
public class AgendaController {

    private final AgendaService agendaService;
    private final VoteService voteService;

    @PostMapping
    @Operation(summary = "Create a new agenda (pauta)")
    public ResponseEntity<VotingDTOs.AgendaResponse> createAgenda(
            @Valid @RequestBody VotingDTOs.AgendaRequest request) {
        log.info("POST /api/v1/agendas - Creating agenda: {}", request.getTitle());
        VotingDTOs.AgendaResponse response = agendaService.createAgenda(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all agendas")
    public ResponseEntity<List<VotingDTOs.AgendaResponse>> listAgendas() {
        log.info("GET /api/v1/agendas - Listing all agendas");
        return ResponseEntity.ok(agendaService.listAllAgendas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agenda by ID")
    public ResponseEntity<VotingDTOs.AgendaResponse> getAgenda(@PathVariable Long id) {
        log.info("GET /api/v1/agendas/{} - Getting agenda", id);
        return ResponseEntity.ok(agendaService.getAgenda(id));
    }

    @PostMapping("/{id}/sessions")
    @Operation(summary = "Open a voting session for an agenda")
    public ResponseEntity<VotingDTOs.AgendaResponse> openSession(
            @PathVariable Long id,
            @RequestBody(required = false) VotingDTOs.OpenSessionRequest request) {
        log.info("POST /api/v1/agendas/{}/sessions - Opening session", id);
        if (request == null) request = new VotingDTOs.OpenSessionRequest();
        VotingDTOs.AgendaResponse response = agendaService.openSession(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/votes")
    @Operation(summary = "Cast a vote on an agenda")
    public ResponseEntity<VotingDTOs.VoteResponse> castVote(
            @PathVariable Long id,
            @Valid @RequestBody VotingDTOs.VoteRequest request) {
        log.info("POST /api/v1/agendas/{}/votes - Casting vote", id);
        VotingDTOs.VoteResponse response = voteService.castVote(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/results")
    @Operation(summary = "Get voting results for an agenda")
    public ResponseEntity<VotingDTOs.VoteResultResponse> getResults(@PathVariable Long id) {
        log.info("GET /api/v1/agendas/{}/results - Getting results", id);
        return ResponseEntity.ok(agendaService.getResult(id));
    }
}
