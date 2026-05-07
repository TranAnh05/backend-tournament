package com.example.tournament.repository;

import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    // Lấy danh sách giải đấu theo trạng thái (cho VĐV/Manager)
    Page<Tournament> findByStatusNot(TournamentStatus status, Pageable pageable);

    // Tìm kiếm theo tên (nếu cần)
    Page<Tournament> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @Query("SELECT t FROM Tournament t " +
            "JOIN FETCH t.sport " +
            "JOIN FETCH t.venue " +
            "WHERE (:isAdmin = true OR t.status <> 'DRAFT') " +
            "AND (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Tournament> findAllWithFilters(@Param("isAdmin") boolean isAdmin,
                                        @Param("name") String name, Pageable pageable);
}
