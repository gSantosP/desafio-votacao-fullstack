package com.voting;

import com.voting.dto.VotingDTOs;
import com.voting.exception.AgendaNotFoundException;
import com.voting.exception.SessionAlreadyOpenException;
import com.voting.model.Agenda;
import com.voting.model.VotingSession;
import com.voting.repository.AgendaRepository;
import com.voting.repository.VoteRepository;
import com.voting.repository.VotingSessionRepository;
import com.voting.service.AgendaService;
import com.voting.service.VoteResultCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgendaService Tests")
class AgendaServiceTest {

    @Mock private AgendaRepository agendaRepository;
    @Mock private VotingSessionRepository sessionRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private VoteResultCacheService cacheService;

    @InjectMocks
    private AgendaService agendaService;

    private Agenda agenda;

    @BeforeEach
    void setUp() {
        agenda = Agenda.builder()
                .id(1L)
                .title("Test Agenda")
                .description("Test Description")
                .status(Agenda.AgendaStatus.CREATED)
                .build();
    }

    @Test
    @DisplayName("Should create agenda successfully")
    void shouldCreateAgenda() {
        VotingDTOs.AgendaRequest request = new VotingDTOs.AgendaRequest("Test Agenda", "Description");
        when(agendaRepository.save(any())).thenReturn(agenda);

        VotingDTOs.AgendaResponse response = agendaService.createAgenda(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Agenda");
        verify(agendaRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw AgendaNotFoundException when agenda does not exist")
    void shouldThrowWhenAgendaNotFound() {
        when(agendaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.getAgenda(999L))
                .isInstanceOf(AgendaNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should open voting session with default duration (1 minute)")
    void shouldOpenSessionWithDefaultDuration() {
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(sessionRepository.findByAgendaId(1L)).thenReturn(Optional.empty());
        when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(agendaRepository.save(any())).thenReturn(agenda);

        agendaService.openSession(1L, new VotingDTOs.OpenSessionRequest(null));

        verify(sessionRepository).save(argThat(session ->
                session.getEndTime().isAfter(LocalDateTime.now().plusSeconds(55)) &&
                session.getEndTime().isBefore(LocalDateTime.now().plusSeconds(65))
        ));
    }

    @Test
    @DisplayName("Should throw SessionAlreadyOpenException when session already exists")
    void shouldThrowWhenSessionAlreadyExists() {
        VotingSession existingSession = VotingSession.builder()
                .id(1L).agenda(agenda)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(5))
                .build();

        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(sessionRepository.findByAgendaId(1L)).thenReturn(Optional.of(existingSession));

        assertThatThrownBy(() -> agendaService.openSession(1L, new VotingDTOs.OpenSessionRequest()))
                .isInstanceOf(SessionAlreadyOpenException.class);
    }

    @Test
    @DisplayName("Should list all agendas")
    void shouldListAllAgendas() {
        // agenda sem sessão: toResponse() não chama voteRepository, sem stubs necessários
        when(agendaRepository.findAllWithSessions()).thenReturn(List.of(agenda));

        List<VotingDTOs.AgendaResponse> result = agendaService.listAllAgendas();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Agenda");
        assertThat(result.get(0).getStatus()).isEqualTo(Agenda.AgendaStatus.CREATED);
    }
}
