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
@Table(name = "sports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

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

    // Quan hệ 1-N: 1 Môn thể thao có nhiều cấu hình luật
    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SportRule> rules = new ArrayList<>();

    @ManyToMany(mappedBy = "supportedSports")
    @Builder.Default
    private Set<Court> courts = new HashSet<>();

    public void addRule(SportRule rule) {
        rules.add(rule);
        rule.setSport(this);
    }

    public void removeRule(SportRule rule) {
        rules.remove(rule);
        rule.setSport(null);
    }
}
