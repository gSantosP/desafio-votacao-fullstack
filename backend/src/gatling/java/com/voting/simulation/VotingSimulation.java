package com.voting.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling Performance Simulation — Voting System
 *
 * Scenarios (choose via -DSCENARIO=smoke|load|stress):
 *   smoke  — 10 users, 30s         — sanidade básica
 *   load   — 500 users, ~5min      — assembleia realista
 *   stress — 2.000 users, ~5min    — encontrar ponto de ruptura
 *
 * Como rodar:
 *   mvn gatling:test                        (smoke, padrão)
 *   mvn gatling:test -DSCENARIO=load
 *   mvn gatling:test -DSCENARIO=stress
 *   mvn gatling:test -DBASE_URL=http://prod-server:8080 -DSCENARIO=load
 *
 * Relatório gerado em: target/gatling/
 */
public class VotingSimulation extends Simulation {

    // ── Config ────────────────────────────────────────────────────────────────

    private static final String BASE_URL = System.getProperty("BASE_URL", "http://localhost:8080");
    private static final String SCENARIO = System.getProperty("SCENARIO", "smoke").toLowerCase();

    // ── HTTP protocol ─────────────────────────────────────────────────────────

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling-VotingSystem/1.0")
            .maxConnectionsPerHost(100)
            .shareConnections();

    // ── Feeders ───────────────────────────────────────────────────────────────

    // Feeder de CPFs para votos — 200k entradas únicas, circula quando esgota
    private static List<Map<String, Object>> buildCpfList() {
        List<Map<String, Object>> list = new ArrayList<>(200_000);
        for (long i = 10_000_000_001L; i < 10_000_200_001L; i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("voteCpf", String.valueOf(i)); // chave "voteCpf" para não colidir com "cpf" da pauta
            list.add(entry);
        }
        return list;
    }

    private final FeederBuilder<Object> cpfFeeder = listFeeder(buildCpfList()).circular();

    // ── Requests reutilizáveis ────────────────────────────────────────────────

    // 1. Cria pauta e salva o id na sessão Gatling como "agendaId"
    private final ChainBuilder createAgenda =
            exec(http("1 - Create Agenda")
                    .post("/api/v1/agendas")
                    .body(StringBody("{\"title\": \"Pauta Gatling\", \"description\": \"Teste de performance\"}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").ofLong().saveAs("agendaId")));

    // 2. Abre sessão na pauta recém-criada (usa agendaId salvo no passo anterior)
    private final ChainBuilder openSession =
            exec(http("2 - Open Session")
                    .post("/api/v1/agendas/#{agendaId}/sessions")
                    .body(StringBody("{\"durationMinutes\": 10}"))
                    .check(status().is(201)));

    // 3. Vota — feed injeta um CPF único por chamada via "voteCpf"
    // Respostas esperadas de negócio (não são falhas de infra):
    //   201 = voto registrado com sucesso
    //   403 = CPF UNABLE_TO_VOTE (facade aleatório)
    //   404 = CPF inválido (facade aleatório)
    //   409 = voto duplicado
    //   422 = sessão fechada
    private final ChainBuilder castVote =
            feed(cpfFeeder)
            .exec(http("3 - Cast Vote")
                    .post("/api/v1/agendas/#{agendaId}/votes")
                    .body(StringBody("{\"cpf\": \"#{voteCpf}\", \"choice\": \"YES\"}"))
                    .checkIf(session -> true).then(status().in(201, 403, 404, 409, 422)));

    // 4. Lê resultado da pauta
    private final ChainBuilder getResults =
            exec(http("4 - Get Results")
                    .get("/api/v1/agendas/#{agendaId}/results")
                    .check(status().is(200)));

    // Leitura simples — para o cenário de observadores
    private final ChainBuilder listAgendas =
            exec(http("List Agendas")
                    .get("/api/v1/agendas")
                    .check(status().is(200)));

    // ── Cenários ──────────────────────────────────────────────────────────────

    /**
     * Fluxo completo por usuário virtual:
     *   1. Cria uma pauta própria
     *   2. Abre sessão de 10 min
     *   3. Vota 5 vezes com CPFs diferentes (feed garante unicidade)
     *   4. Lê o resultado
     *
     * Cada usuário opera na sua própria pauta — sem conflito de agendaId entre usuários.
     */
    private final ScenarioBuilder fullVotingFlow = scenario("Full Voting Flow")
            .exec(createAgenda)
            .pause(Duration.ofMillis(200))
            .exec(openSession)
            .pause(Duration.ofMillis(100))
            .repeat(5).on(
                    exec(castVote)
                    .pause(Duration.ofMillis(50))
            )
            .exec(getResults);

    /**
     * Apenas leitura — simula observadores / dashboard polilng.
     * Não cria pautas, apenas lista e lê resultados da pauta 1.
     */
    private final ScenarioBuilder readOnlyFlow = scenario("Read-Only Observer")
            .exec(listAgendas)
            .pause(Duration.ofMillis(300))
            .exec(http("Poll Results")
                    .get("/api/v1/agendas/1/results")
                    .checkIf(session -> true).then(status().in(200, 404, 422)));

    /**
     * Stress no endpoint de voto — vota em pautas já existentes.
     * Requer que o smoke/load já tenha criado pautas com id >= 1.
     */
    private final ScenarioBuilder voteOnlyFlow = scenario("Vote Only")
            .feed(cpfFeeder)
            .exec(session -> session.set("agendaId", 1L)) // vota sempre na pauta 1
            .exec(castVote);

    // ── Perfis de carga ───────────────────────────────────────────────────────

    private PopulationBuilder smokeTest() {
        return fullVotingFlow.injectOpen(
                rampUsers(10).during(Duration.ofSeconds(10))
        );
    }

    private PopulationBuilder[] loadTest() {
        return new PopulationBuilder[]{
                fullVotingFlow.injectOpen(
                        nothingFor(Duration.ofSeconds(5)),
                        rampUsers(350).during(Duration.ofMinutes(2)),
                        constantUsersPerSec(50).during(Duration.ofMinutes(3))
                ),
                readOnlyFlow.injectOpen(
                        nothingFor(Duration.ofSeconds(5)),
                        rampUsers(150).during(Duration.ofMinutes(2)),
                        constantUsersPerSec(20).during(Duration.ofMinutes(3))
                )
        };
    }

    private PopulationBuilder[] stressTest() {
        return new PopulationBuilder[]{
                fullVotingFlow.injectOpen(
                        rampUsers(500).during(Duration.ofSeconds(30)),
                        rampUsers(1000).during(Duration.ofMinutes(1)),
                        rampUsers(2000).during(Duration.ofMinutes(2))
                ),
                voteOnlyFlow.injectOpen(
                        nothingFor(Duration.ofMinutes(1)),
                        constantUsersPerSec(100).during(Duration.ofMinutes(3))
                )
        };
    }

    // ── Thresholds ────────────────────────────────────────────────────────────

    private static final Assertion[] ASSERTIONS = {
            global().responseTime().percentile(99).lt(2000),  // P99 < 2s
            global().responseTime().percentile(95).lt(500),   // P95 < 500ms
            global().responseTime().mean().lt(200),           // média < 200ms
    };

    // ── Setup ─────────────────────────────────────────────────────────────────

    {
        switch (SCENARIO) {
            case "load"   -> setUp(loadTest()).protocols(httpProtocol).assertions(ASSERTIONS);
            case "stress" -> setUp(stressTest()).protocols(httpProtocol).assertions(ASSERTIONS);
            default       -> setUp(smokeTest()).protocols(httpProtocol).assertions(ASSERTIONS);
        }
    }
}
