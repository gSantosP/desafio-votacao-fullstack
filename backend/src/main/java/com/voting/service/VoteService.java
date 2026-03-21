package com.voting.service;

import com.voting.dto.VotingDTOs;
import com.voting.exception.DuplicateVoteException;
import com.voting.exception.UnableToVoteException;
import com.voting.exception.VotingSessionClosedException;
import com.voting.facade.CpfValidationFacade;
import com.voting.model.Agenda;
import com.voting.model.Vote;
import com.voting.model.VotingSession;
import com.voting.repository.VoteRepository;
import com.voting.repository.VotingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {

    private final VoteRepository voteRepository;
    private final VotingSessionRepository sessionRepository;
    private final AgendaService agendaService;
    private final CpfValidationFacade cpfValidationFacade;
    private final VoteResultCacheService cacheService;

    @Transactional
    public VotingDTOs.VoteResponse castVote(Long agendaId, VotingDTOs.VoteRequest request) {
        String cpf = request.getCpf().replaceAll("[^0-9]", "");
        log.info("Processing vote for agenda {} from CPF {}", agendaId, maskCpf(cpf));

        // Step 1: Validate CPF via external facade (Bonus Task 1)
        VotingDTOs.CpfValidationResponse cpfValidation = cpfValidationFacade.validate(cpf);
        if ("UNABLE_TO_VOTE".equals(cpfValidation.getStatus())) {
            throw new UnableToVoteException(maskCpf(cpf));
        }

        // Step 2: Find agenda
        Agenda agenda = agendaService.findAgendaById(agendaId);

        // Step 3: Validate session is open
        VotingSession session = sessionRepository.findByAgendaId(agendaId)
                .orElseThrow(() -> new VotingSessionClosedException(agendaId));

        if (!session.isOpen()) {
            throw new VotingSessionClosedException(agendaId);
        }

        // Step 4: Check for duplicate vote
        if (voteRepository.existsByAgendaIdAndAssociateCpf(agendaId, cpf)) {
            throw new DuplicateVoteException(maskCpf(cpf), agendaId);
        }

        // Step 5: Persist vote
        Vote vote = Vote.builder()
                .agenda(agenda)
                .associateCpf(cpf)
                .choice(request.getChoice())
                .build();

        Vote saved = voteRepository.save(vote);
        // Evict cached result so next GET /results reflects this vote
        cacheService.evictResult(agendaId);
        log.info("Vote cast successfully for agenda {} by CPF {}: {}",
                agendaId, maskCpf(cpf), request.getChoice());

        return VotingDTOs.VoteResponse.builder()
                .id(saved.getId())
                .agendaId(agenda.getId())
                .agendaTitle(agenda.getTitle())
                .maskedCpf(maskCpf(cpf))
                .choice(saved.getChoice())
                .votedAt(saved.getVotedAt())
                .build();
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) return "***";
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}
