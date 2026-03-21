package com.voting.service;

import com.voting.dto.VotingDTOs;
import com.voting.model.Agenda;
import com.voting.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cache layer for vote result reads.
 *
 * Problem: In a high-volume scenario (100k+ votes), the GET /results endpoint
 * would hammer the DB with COUNT queries on every request. With a live session
 * being polled by many observers simultaneously, this becomes a bottleneck.
 *
 * Solution: Cache results for closed sessions (immutable) indefinitely,
 * and short-TTL cache for open sessions is handled by the simple in-memory
 * cache (Spring Cache with CaffeineCache can be added for TTL support).
 *
 * Cache eviction: triggered whenever a new vote is cast for that agenda.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoteResultCacheService {

    private final VoteRepository voteRepository;

    /**
     * Returns cached vote counts for a given agenda.
     * Cache key = agendaId. Evicted on every new vote (see evictResult).
     *
     * For CLOSED agendas this is effectively permanent (result never changes).
     * For OPEN agendas it's evicted after each vote, so observers see
     * near-real-time counts without hammering the DB.
     */
    @Cacheable(value = "voteResults", key = "#agendaId")
    public long[] getCounts(Long agendaId) {
        log.debug("Cache MISS for agendaId={}, querying DB", agendaId);
        long yes = voteRepository.countYesVotesByAgendaId(agendaId);
        long no  = voteRepository.countNoVotesByAgendaId(agendaId);
        return new long[]{yes, no};
    }

    /**
     * Evicts cached counts for an agenda after a new vote is cast.
     * Called by VoteService immediately after persisting a vote.
     */
    @CacheEvict(value = "voteResults", key = "#agendaId")
    public void evictResult(Long agendaId) {
        log.debug("Cache EVICT for agendaId={}", agendaId);
    }

    public VotingDTOs.VoteResultResponse buildResult(
            Long agendaId, String title, Agenda.AgendaStatus status, boolean sessionOpen) {

        long[] counts = getCounts(agendaId);
        long yes   = counts[0];
        long no    = counts[1];
        long total = yes + no;

        String winner = total == 0 ? "NO_VOTES"
                : yes > no ? "YES"
                : no > yes ? "NO"
                : "TIE";

        return VotingDTOs.VoteResultResponse.builder()
                .agendaId(agendaId)
                .agendaTitle(title)
                .agendaStatus(status)
                .totalVotes(total)
                .yesVotes(yes)
                .noVotes(no)
                .winner(winner)
                .sessionOpen(sessionOpen)
                .build();
    }
}
