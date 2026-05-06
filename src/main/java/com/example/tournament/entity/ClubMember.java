package com.example.tournament.entity;

import com.example.tournament.enums.ClubRole;
import com.example.tournament.enums.JoinStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_status", length = 20)
    @Builder.Default
    private JoinStatus joinStatus = JoinStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "club_role", length = 20)
    @Builder.Default
    private ClubRole clubRole = ClubRole.MEMBER;

    @Column(name = "joined_date")
    private LocalDateTime joinedDate;

    @Column(name = "left_date")
    private LocalDateTime leftDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
