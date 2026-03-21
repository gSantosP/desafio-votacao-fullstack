package com.voting.repository;

import com.voting.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByAgendaIdAndAssociateCpf(Long agendaId, String associateCpf);

    Optional<Vote> findByAgendaIdAndAssociateCpf(Long agendaId, String associateCpf);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.agenda.id = :agendaId AND v.choice = 'YES'")
    long countYesVotesByAgendaId(@Param("agendaId") Long agendaId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.agenda.id = :agendaId AND v.choice = 'NO'")
    long countNoVotesByAgendaId(@Param("agendaId") Long agendaId);

    long countByAgendaId(Long agendaId);
}
