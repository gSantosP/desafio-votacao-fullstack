package com.voting.repository;

import com.voting.model.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {

    Optional<VotingSession> findByAgendaId(Long agendaId);

    List<VotingSession> findByEndTimeBefore(LocalDateTime dateTime);
}
