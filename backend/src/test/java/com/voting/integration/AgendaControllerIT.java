package com.voting.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voting.dto.VotingDTOs;
import com.voting.facade.CpfValidationFacade;
import com.voting.model.Agenda;
import com.voting.model.Vote;
import com.voting.repository.AgendaRepository;
import com.voting.repository.VoteRepository;
import com.voting.repository.VotingSessionRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Voting System API.
 *
 * Uses @SpringBootTest to load the full application context with H2 in-memory.
 * Mocks CpfValidationFacade to control CPF validation behavior deterministically.
 * Tests run in order to simulate a realistic voting session lifecycle.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Voting System Integration Tests")
class AgendaControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AgendaRepository agendaRepository;
    @Autowired VotingSessionRepository sessionRepository;
    @Autowired VoteRepository voteRepository;

    // Mock the CPF facade so tests are deterministic (no randomness)
    @MockBean CpfValidationFacade cpfValidationFacade;

    // Shared state across ordered tests
    private static Long agendaId;

    @BeforeEach
    void setUp() {
        // Always return ABLE_TO_VOTE unless overridden per test
        when(cpfValidationFacade.validate(anyString()))
                .thenReturn(new VotingDTOs.CpfValidationResponse("ABLE_TO_VOTE"));
    }

    @AfterAll
    static void cleanUp(@Autowired AgendaRepository agendaRepo,
                        @Autowired VoteRepository voteRepo,
                        @Autowired VotingSessionRepository sessionRepo) {
        voteRepo.deleteAll();
        sessionRepo.deleteAll();
        agendaRepo.deleteAll();
    }

    // ── Agenda ────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("POST /agendas — should create agenda and return 201")
    void shouldCreateAgenda() throws Exception {
        VotingDTOs.AgendaRequest request = new VotingDTOs.AgendaRequest(
                "Reforma do Estatuto", "Votação sobre alterações no estatuto social");

        MvcResult result = mockMvc.perform(post("/api/v1/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Reforma do Estatuto"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.session").doesNotExist())
                .andReturn();

        agendaId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("POST /agendas — should return 400 when title is blank")
    void shouldReturn400WhenTitleBlank() throws Exception {
        VotingDTOs.AgendaRequest request = new VotingDTOs.AgendaRequest("", null);

        mockMvc.perform(post("/api/v1/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /agendas — should list all agendas")
    void shouldListAgendas() throws Exception {
        mockMvc.perform(get("/api/v1/agendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title").isString());
    }

    @Test
    @Order(4)
    @DisplayName("GET /agendas/{id} — should return agenda by id")
    void shouldGetAgendaById() throws Exception {
        mockMvc.perform(get("/api/v1/agendas/{id}", agendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendaId))
                .andExpect(jsonPath("$.title").value("Reforma do Estatuto"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /agendas/999 — should return 404 for unknown agenda")
    void shouldReturn404ForUnknownAgenda() throws Exception {
        mockMvc.perform(get("/api/v1/agendas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ── Session ───────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("POST /agendas/{id}/sessions — should open session with default duration")
    void shouldOpenSession() throws Exception {
        mockMvc.perform(post("/api/v1/agendas/{id}/sessions", agendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.session").exists())
                .andExpect(jsonPath("$.session.open").value(true));
    }

    @Test
    @Order(7)
    @DisplayName("POST /agendas/{id}/sessions — should return 409 when session already exists")
    void shouldReturn409WhenSessionAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/v1/agendas/{id}/sessions", agendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @Order(8)
    @DisplayName("POST /agendas/{id}/sessions — should open session with custom duration")
    void shouldOpenSessionWithCustomDuration() throws Exception {
        // Create a separate agenda for this test
        MvcResult result = mockMvc.perform(post("/api/v1/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Pauta Custom Duration\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        long newId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/v1/agendas/{id}/sessions", newId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMinutes\": 5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.session.open").value(true));
    }

    // ── Votes ─────────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("POST /agendas/{id}/votes — should cast YES vote successfully")
    void shouldCastYesVote() throws Exception {
        VotingDTOs.VoteRequest request = new VotingDTOs.VoteRequest("52998224725", Vote.VoteChoice.YES);

        mockMvc.perform(post("/api/v1/agendas/{id}/votes", agendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.choice").value("YES"))
                .andExpect(jsonPath("$.agendaId").value(agendaId))
                .andExpect(jsonPath("$.maskedCpf").isString());
    }

    @Test
    @Order(10)
    @DisplayName("POST /agendas/{id}/votes — should cast NO vote successfully")
    void shouldCastNoVote() throws Exception {
        VotingDTOs.VoteRequest request = new VotingDTOs.VoteRequest("11144477735", Vote.VoteChoice.NO);

        mockMvc.perform(post("/api/v1/agendas/{id}/votes", agendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.choice").value("NO"));
    }

    @Test
    @Order(11)
    @DisplayName("POST /agendas/{id}/votes — should return 409 on duplicate vote")
    void shouldReturn409OnDuplicateVote() throws Exception {
        VotingDTOs.VoteRequest request = new VotingDTOs.VoteRequest("52998224725", Vote.VoteChoice.NO);

        mockMvc.perform(post("/api/v1/agendas/{id}/votes", agendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Vote"));
    }

    @Test
    @Order(12)
    @DisplayName("POST /agendas/{id}/votes — should return 403 when CPF is UNABLE_TO_VOTE")
    void shouldReturn403WhenUnableToVote() throws Exception {
        when(cpfValidationFacade.validate(anyString()))
                .thenReturn(new VotingDTOs.CpfValidationResponse("UNABLE_TO_VOTE"));

        VotingDTOs.VoteRequest request = new VotingDTOs.VoteRequest("47123586058", Vote.VoteChoice.YES);

        mockMvc.perform(post("/api/v1/agendas/{id}/votes", agendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    @DisplayName("POST /agendas/{id}/votes — should return 404 when CPF is invalid")
    void shouldReturn404WhenCpfInvalid() throws Exception {
        when(cpfValidationFacade.validate(anyString()))
                .thenThrow(new CpfValidationFacade.CpfNotFoundException("Invalid CPF"));

        VotingDTOs.VoteRequest request = new VotingDTOs.VoteRequest("00000000000", Vote.VoteChoice.YES);

        mockMvc.perform(post("/api/v1/agendas/{id}/votes", agendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── Results ───────────────────────────────────────────────────────────────

    @Test
    @Order(14)
    @DisplayName("GET /agendas/{id}/results — should return correct vote counts")
    void shouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/v1/agendas/{id}/results", agendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(2))
                .andExpect(jsonPath("$.yesVotes").value(1))
                .andExpect(jsonPath("$.noVotes").value(1))
                .andExpect(jsonPath("$.winner").value("TIE"))
                .andExpect(jsonPath("$.sessionOpen").value(true));
    }

    @Test
    @Order(15)
    @DisplayName("GET /agendas/999/results — should return 404 for unknown agenda")
    void shouldReturn404ForUnknownAgendaResults() throws Exception {
        mockMvc.perform(get("/api/v1/agendas/999/results"))
                .andExpect(status().isNotFound());
    }
}
