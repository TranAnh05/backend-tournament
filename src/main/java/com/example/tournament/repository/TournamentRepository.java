package com.example.tournament.repository;

import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    @Query("SELECT t FROM Tournament t " +
            "JOIN FETCH t.sport " +
            "JOIN FETCH t.venue " +
            "WHERE t.id = :id")
    Optional<Tournament> findByIdWithDetails(@Param("id") Long id);
    // Tìm kiếm theo tên (nếu cần)
    Page<Tournament> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @Query("SELECT t FROM Tournament t " +
            "JOIN FETCH t.sport " +
            "JOIN FETCH t.venue " +
            "WHERE (:isAdmin = true OR t.status <> 'DRAFT') " +
            "AND (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Tournament> findAllWithFilters(@Param("isAdmin") boolean isAdmin,
                                        @Param("name") String name, Pageable pageable);

    // ADMIN
    // Kiểm tra xem có giải đấu nào của môn thể thao nằm trong danh sách các status chỉ định không
    boolean existsBySportIdAndStatusIn(Long sportId, List<TournamentStatus> statuses);

    @Query("SELECT COUNT(t) > 0 FROM Tournament t WHERE t.venue.id = :venueId " +
            "AND t.status IN ('DRAFT', 'REGISTRATION_OPEN', 'ONGOING')")
    boolean hasActiveTournamentsAtVenue(@Param("venueId") Long venueId);

    // Đếm tổng số giải đấu do BTC quản lý
    long countByOrganizerId(Long organizerId);

    // Đếm tổng số CÂU LẠC BỘ đã từng tham gia các giải của BTC này
    @Query("SELECT COUNT(DISTINCT tr.club.id) FROM TournamentRegistration tr " +
            "WHERE tr.tournament.organizer.id = :organizerId AND tr.status = 'APPROVED'")
    long countDistinctClubsByOrganizerId(@Param("organizerId") Long organizerId);

    // Lấy 5 giải đấu gần nhất do BTC này tạo
    List<Tournament> findTop5ByOrganizerIdOrderByCreatedAtDesc(Long organizerId);
    // ============================================================================
}
