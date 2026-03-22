package com.voting;

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
import com.voting.service.AgendaService;
import com.voting.service.VoteResultCacheService;
import com.voting.service.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService Tests")
class VoteServiceTest {

    @Mock private VoteRepository voteRepository;
    @Mock private VotingSessionRepository sessionRepository;
    @Mock private AgendaService agendaService;
    @Mock private CpfValidationFacade cpfValidationFacade;
    @Mock private VoteResultCacheService cacheService; // foi adicionado depois — estava faltando

    @InjectMocks
    private VoteService voteService;

    private Agenda agenda;
    private VotingSession openSession;
    private VotingDTOs.VoteRequest voteRequest;

    @BeforeEach
    void setUp() {
        agenda = Agenda.builder()
                .id(1L).title("Test Agenda")
                .status(Agenda.AgendaStatus.OPEN)
                .build();

        openSession = VotingSession.builder()
                .id(1L).agenda(agenda)
                .startTime(LocalDateTime.now().minusSeconds(10))
                .endTime(LocalDateTime.now().plusMinutes(5))
                .build();

        voteRequest = new VotingDTOs.VoteRequest("12345678901", Vote.VoteChoice.YES);
    }

    @Test
    @DisplayName("Should cast vote successfully")
    void shouldCastVoteSuccessfully() {
        when(cpfValidationFacade.validate(any()))
                .thenReturn(new VotingDTOs.CpfValidationResponse("ABLE_TO_VOTE"));
        when(agendaService.findAgendaById(1L)).thenReturn(agenda);
        when(sessionRepository.findByAgendaId(1L)).thenReturn(Optional.of(openSession));
        when(voteRepository.existsByAgendaIdAndAssociateCpf(any(), any())).thenReturn(false);
        when(voteRepository.save(any())).thenAnswer(inv -> {
            Vote v = inv.getArgument(0);
            return Vote.builder().id(1L).agenda(agenda)
                    .associateCpf("12345678901").choice(v.getChoice()).build();
        });
        // cacheService.evictResult é void — Mockito não precisa de stubbing para void por padrão

        VotingDTOs.VoteResponse response = voteService.castVote(1L, voteRequest);

        assertThat(response).isNotNull();
        assertThat(response.getChoice()).isEqualTo(Vote.VoteChoice.YES);
        verify(voteRepository).save(any());
        verify(cacheService).evictResult(1L); // garante que o cache é invalidado
    }

    @Test
    @DisplayName("Should throw UnableToVoteException when CPF is UNABLE_TO_VOTE")
    void shouldThrowWhenUnableToVote() {
        when(cpfValidationFacade.validate(any()))
                .thenReturn(new VotingDTOs.CpfValidationResponse("UNABLE_TO_VOTE"));

        assertThatThrownBy(() -> voteService.castVote(1L, voteRequest))
                .isInstanceOf(UnableToVoteException.class);
    }

    @Test
    @DisplayName("Should throw DuplicateVoteException when voting twice")
    void shouldThrowOnDuplicateVote() {
        when(cpfValidationFacade.validate(any()))
                .thenReturn(new VotingDTOs.CpfValidationResponse("ABLE_TO_VOTE"));
        when(agendaService.findAgendaById(1L)).thenReturn(agenda);
        when(sessionRepository.findByAgendaId(1L)).thenReturn(Optional.of(openSession));
        when(voteRepository.existsByAgendaIdAndAssociateCpf(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> voteService.castVote(1L, voteRequest))
                .isInstanceOf(DuplicateVoteException.class);
    }

    @Test
    @DisplayName("Should throw VotingSessionClosedException when session is closed")
    void shouldThrowWhenSessionClosed() {
        VotingSession closedSession = VotingSession.builder()
                .id(1L).agenda(agenda)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now().minusMinutes(5))
                .build();

        when(cpfValidationFacade.validate(any()))
                .thenReturn(new VotingDTOs.CpfValidationResponse("ABLE_TO_VOTE"));
        when(agendaService.findAgendaById(1L)).thenReturn(agenda);
        when(sessionRepository.findByAgendaId(1L)).thenReturn(Optional.of(closedSession));

        assertThatThrownBy(() -> voteService.castVote(1L, voteRequest))
                .isInstanceOf(VotingSessionClosedException.class);
    }
}
