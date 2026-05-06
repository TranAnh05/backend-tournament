package com.example.tournament.entity;

import com.example.tournament.enums.CommonStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clubs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "short_name", nullable = false, length = 50)
    private String shortName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String headquarters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_venue_id")
    private Venue homeVenue;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "homeClub")
    @Builder.Default
    private List<Match> homeMatches = new ArrayList<>();

    @OneToMany(mappedBy = "awayClub")
    @Builder.Default
    private List<Match> awayMatches = new ArrayList<>();

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchLineup> matchLineups = new ArrayList<>();

    @OneToMany(mappedBy = "club")
    @Builder.Default
    private List<MatchEvent> matchEvents = new ArrayList<>();

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Standing> standings = new ArrayList<>();

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlayerStatistic> playerStatistics = new ArrayList<>();

    @OneToMany(mappedBy = "club")
    @Builder.Default
    private List<Discipline> disciplines = new ArrayList<>();
}
