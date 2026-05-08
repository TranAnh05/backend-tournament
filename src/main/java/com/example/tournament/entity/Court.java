package com.example.tournament.entity;

import com.example.tournament.enums.CommonStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "courts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "court_name", nullable = false, length = 100)
    private String courtName;

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

    // Quan hệ N-N với Sport thông qua bảng trung gian
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "court_supported_sports",
            joinColumns = @JoinColumn(name = "court_id"),
            inverseJoinColumns = @JoinColumn(name = "sport_id")
    )
    @Builder.Default
    private Set<Sport> supportedSports = new HashSet<>();

    @OneToMany(mappedBy = "court")
    @Builder.Default
    private List<Match> matches = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
