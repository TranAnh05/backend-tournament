package com.example.tournament.entity;

import com.example.tournament.enums.RosterRole;
import com.example.tournament.enums.RosterStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
// Unique Key: Một giải đấu chỉ cho phép một VĐV đăng ký 1 lần (dù họ có ở CLB nào)
@Table(name = "tournament_rosters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_roster_tour_athlete", columnNames = {"tournament_id", "athlete_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentRoster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(length = 100)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private RosterRole role = RosterRole.PLAYER;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RosterStatus status = RosterStatus.ELIGIBLE;

    @CreationTimestamp
    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;
}
