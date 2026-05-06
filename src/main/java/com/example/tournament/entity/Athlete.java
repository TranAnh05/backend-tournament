package com.example.tournament.entity;

import com.example.tournament.enums.HealthStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "athletes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Athlete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(name = "identity_number", nullable = false, unique = true, length = 50)
    private String identityNumber;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "portrait_url", length = 500)
    private String portraitUrl;

    @Column(name = "preferred_number")
    private Integer preferredNumber;

    @Column(name = "preferred_position", length = 100)
    private String preferredPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", length = 20)
    @Builder.Default
    private HealthStatus healthStatus = HealthStatus.FIT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "athlete", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchLineup> matchLineups = new ArrayList<>();

    @OneToMany(mappedBy = "primaryAthlete")
    @Builder.Default
    private List<MatchEvent> primaryEvents = new ArrayList<>();

    @OneToMany(mappedBy = "secondaryAthlete")
    @Builder.Default
    private List<MatchEvent> secondaryEvents = new ArrayList<>();

    @OneToMany(mappedBy = "athlete", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlayerStatistic> statistics = new ArrayList<>();

    @OneToMany(mappedBy = "athlete")
    @Builder.Default
    private List<Discipline> disciplines = new ArrayList<>();
}
