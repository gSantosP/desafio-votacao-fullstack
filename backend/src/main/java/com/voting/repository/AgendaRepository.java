package com.voting.repository;

import com.voting.model.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, Long> {

    @Query("SELECT a FROM Agenda a LEFT JOIN FETCH a.session ORDER BY a.createdAt DESC")
    List<Agenda> findAllWithSessions();
}
