package com.example.tournament.entity;

import com.example.tournament.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_registrations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_registration_tour_club", columnNames = {"tournament_id", "club_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "financial_proof_url", length = 500)
    private String financialProofUrl;

    @Column(name = "home_kit_color", nullable = false, length = 50)
    private String homeKitColor;

    @Column(name = "away_kit_color", length = 50)
    private String awayKitColor;

    @CreationTimestamp
    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;
}
