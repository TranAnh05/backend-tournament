package com.example.tournament.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_statistics", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stat_tournament_athlete", columnNames = {"tournament_id", "athlete_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "matches_played")
    @Builder.Default
    private Integer matchesPlayed = 0;

    @Column(name = "scores")
    @Builder.Default
    private Integer scores = 0;

    @Column(name = "assists")
    @Builder.Default
    private Integer assists = 0;

    @Column(name = "fouls")
    @Builder.Default
    private Integer fouls = 0;

    @Column(name = "mvp_count")
    @Builder.Default
    private Integer mvpCount = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
