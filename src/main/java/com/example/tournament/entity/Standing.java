package com.example.tournament.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "standings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_standings_group_club", columnNames = {"group_stage_id", "club_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Standing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_stage_id", nullable = false)
    private GroupStage groupStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "matches_played")
    @Builder.Default
    private Integer matchesPlayed = 0;

    @Column(name = "matches_won")
    @Builder.Default
    private Integer matchesWon = 0;

    @Column(name = "matches_drawn")
    @Builder.Default
    private Integer matchesDrawn = 0;

    @Column(name = "matches_lost")
    @Builder.Default
    private Integer matchesLost = 0;

    @Column(name = "scores_for")
    @Builder.Default
    private Integer scoresFor = 0;

    @Column(name = "scores_against")
    @Builder.Default
    private Integer scoresAgainst = 0;

    @Column(name = "score_difference")
    @Builder.Default
    private Integer scoreDifference = 0;

    @Column(name = "total_points")
    @Builder.Default
    private Integer totalPoints = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
