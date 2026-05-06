package com.example.tournament.entity;

import com.example.tournament.enums.RoleCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private RoleCode roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Quan hệ 1-N với bảng UserRole (Không bắt buộc phải có tùy nghiệp vụ, nhưng nên định nghĩa để truy xuất ngược)
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();
}
