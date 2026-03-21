package com.voting.service;

import com.voting.dto.VotingDTOs;
import com.voting.exception.AgendaNotFoundException;
import com.voting.exception.SessionAlreadyOpenException;
import com.voting.exception.VotingSessionClosedException;
import com.voting.model.Agenda;
import com.voting.model.VotingSession;
import com.voting.repository.AgendaRepository;
import com.voting.repository.VoteRepository;
import com.voting.repository.VotingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgendaService {

    private static final int DEFAULT_SESSION_DURATION_MINUTES = 1;

    private final AgendaRepository agendaRepository;
    private final VotingSessionRepository sessionRepository;
    private final VoteRepository voteRepository;

    @Transactional
    public VotingDTOs.AgendaResponse createAgenda(VotingDTOs.AgendaRequest request) {
        log.info("Creating agenda: {}", request.getTitle());
        Agenda agenda = Agenda.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        Agenda saved = agendaRepository.save(agenda);
        log.info("Agenda created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VotingDTOs.AgendaResponse> listAllAgendas() {
        return agendaRepository.findAllWithSessions()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VotingDTOs.AgendaResponse getAgenda(Long id) {
        Agenda agenda = findAgendaById(id);
        return toResponse(agenda);
    }

    @Transactional
    public VotingDTOs.AgendaResponse openSession(Long agendaId, VotingDTOs.OpenSessionRequest request) {
        log.info("Opening session for agenda id: {}", agendaId);
        Agenda agenda = findAgendaById(agendaId);

        // Check if session already exists
        if (sessionRepository.findByAgendaId(agendaId).isPresent()) {
            throw new SessionAlreadyOpenException(agendaId);
        }

        int durationMinutes = (request.getDurationMinutes() != null && request.getDurationMinutes() > 0)
                ? request.getDurationMinutes()
                : DEFAULT_SESSION_DURATION_MINUTES;

        LocalDateTime now = LocalDateTime.now();
        VotingSession session = VotingSession.builder()
                .agenda(agenda)
                .startTime(now)
                .endTime(now.plusMinutes(durationMinutes))
                .build();

        sessionRepository.save(session);
        agenda.setStatus(Agenda.AgendaStatus.OPEN);
        agendaRepository.save(agenda);

        log.info("Session opened for agenda {} until {}", agendaId, session.getEndTime());
        return toResponse(agendaRepository.findById(agendaId).orElseThrow());
    }

    @Transactional(readOnly = true)
    public VotingDTOs.VoteResultResponse getResult(Long agendaId) {
        Agenda agenda = findAgendaById(agendaId);
        VotingSession session = sessionRepository.findByAgendaId(agendaId)
                .orElseThrow(() -> new VotingSessionClosedException(agendaId));

        long yesVotes = voteRepository.countYesVotesByAgendaId(agendaId);
        long noVotes = voteRepository.countNoVotesByAgendaId(agendaId);
        long total = yesVotes + noVotes;

        String winner;
        if (total == 0) {
            winner = "NO_VOTES";
        } else if (yesVotes > noVotes) {
            winner = "YES";
        } else if (noVotes > yesVotes) {
            winner = "NO";
        } else {
            winner = "TIE";
        }

        return VotingDTOs.VoteResultResponse.builder()
                .agendaId(agenda.getId())
                .agendaTitle(agenda.getTitle())
                .agendaStatus(agenda.getStatus())
                .totalVotes(total)
                .yesVotes(yesVotes)
                .noVotes(noVotes)
                .winner(winner)
                .sessionOpen(session.isOpen())
                .build();
    }

    /**
     * Scheduled task that closes expired sessions every 30 seconds.
     */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void closeExpiredSessions() {
        List<VotingSession> expiredSessions = sessionRepository.findByEndTimeBefore(LocalDateTime.now());
        expiredSessions.forEach(session -> {
            Agenda agenda = session.getAgenda();
            if (agenda.getStatus() == Agenda.AgendaStatus.OPEN) {
                agenda.setStatus(Agenda.AgendaStatus.CLOSED);
                agendaRepository.save(agenda);
                log.info("Closed session for agenda id: {}", agenda.getId());
            }
        });
    }

    public Agenda findAgendaById(Long id) {
        return agendaRepository.findById(id)
                .orElseThrow(() -> new AgendaNotFoundException(id));
    }

    private VotingDTOs.AgendaResponse toResponse(Agenda agenda) {
        VotingDTOs.SessionResponse sessionResponse = null;
        if (agenda.getSession() != null) {
            VotingSession s = agenda.getSession();
            sessionResponse = VotingDTOs.SessionResponse.builder()
                    .id(s.getId())
                    .startTime(s.getStartTime())
                    .endTime(s.getEndTime())
                    .open(s.isOpen())
                    .createdAt(s.getCreatedAt())
                    .build();
        }

        VotingDTOs.VoteResultResponse result = null;
        if (agenda.getSession() != null) {
            long yes = voteRepository.countYesVotesByAgendaId(agenda.getId());
            long no = voteRepository.countNoVotesByAgendaId(agenda.getId());
            long total = yes + no;
            String winner = total == 0 ? "NO_VOTES" : (yes > no ? "YES" : (no > yes ? "NO" : "TIE"));
            result = VotingDTOs.VoteResultResponse.builder()
                    .agendaId(agenda.getId())
                    .agendaTitle(agenda.getTitle())
                    .agendaStatus(agenda.getStatus())
                    .totalVotes(total)
                    .yesVotes(yes)
                    .noVotes(no)
                    .winner(winner)
                    .sessionOpen(agenda.getSession().isOpen())
                    .build();
        }

        return VotingDTOs.AgendaResponse.builder()
                .id(agenda.getId())
                .title(agenda.getTitle())
                .description(agenda.getDescription())
                .status(agenda.getStatus())
                .session(sessionResponse)
                .result(result)
                .createdAt(agenda.getCreatedAt())
                .updatedAt(agenda.getUpdatedAt())
                .build();
    }
}
