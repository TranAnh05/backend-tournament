package com.example.tournament.repository;

import com.example.tournament.entity.Tournament;
import com.example.tournament.enums.TournamentStatus;
import com.example.tournament.payload.response.admin.AdminActivityTrendProjection;
import com.example.tournament.payload.response.admin.AdminChartDataProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    /**
     * Kiểm tra xem BTC có đang quản lý giải đấu nào chưa kết thúc không.
     * Một giải đấu được coi là đang chạy nếu status KHÔNG PHẢI là 'FINISHED' hoặc 'CANCELED'.
     */
    @Query("SELECT COUNT(t) > 0 FROM Tournament t WHERE t.organizer.id = :organizerId " +
            "AND t.status NOT IN ('FINISHED', 'CANCELED')")
    boolean hasActiveTournaments(@Param("organizerId") Long organizerId);

    // Biểu đồ Tròn: Tỷ lệ môn thể thao
    @Query("SELECT s.name AS label, COUNT(t.id) AS value " +
            "FROM Tournament t JOIN t.sport s " +
            "WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate " +
            "GROUP BY s.id")
    List<AdminChartDataProjection> countTournamentsBySport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Biểu đồ Cột ngang: Mức độ sử dụng địa điểm
    @Query("SELECT v.name AS label, COUNT(t.id) AS value " +
            "FROM Tournament t JOIN t.venue v " +
            "WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate " +
            "GROUP BY v.id " +
            "ORDER BY value DESC")
    List<AdminChartDataProjection> countVenueUsage(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Biểu đồ Đường: Xu hướng tạo giải đấu theo tháng (Sử dụng Native Query cho MySQL)
    @Query(value = "SELECT CONCAT('Tháng ', MONTH(MIN(created_at))) AS timeLabel, COUNT(id) AS newTournaments " +
            "FROM tournaments " +
            "WHERE created_at >= :startDate AND created_at <= :endDate " +
            "GROUP BY YEAR(created_at), MONTH(created_at) " +
            "ORDER BY YEAR(created_at) ASC, MONTH(created_at) ASC",
            nativeQuery = true)
    List<AdminActivityTrendProjection> countActivityTrends(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    // ============================================================================

    List<Tournament> findByStatus(TournamentStatus status);

    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);

    // ORGANINER
    @Query("SELECT t FROM Tournament t " +
            "JOIN FETCH t.sport s " +
            "WHERE t.organizer.id = :organizerId " +
            "AND t.isDeleted = false " +
            "AND t.status IN ( 'REGISTRATION_CLOSE','ONGOING' ) " +
            "ORDER BY t.createdAt DESC")
    List<Tournament> findTournamentsForStandingsLookup(@Param("organizerId") Long organizerId);
}
