package com.voting.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "votes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"agenda_id", "associate_cpf"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;

    @Column(name = "associate_cpf", nullable = false)
    private String associateCpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteChoice choice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime votedAt;

    public enum VoteChoice {
        YES, NO
    }
}
