package com.example.tournament.repository;

import com.example.tournament.entity.MatchReferee;
import com.example.tournament.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRefereeRepository extends JpaRepository<MatchReferee, Long> {

    // REFEREE
    // Truy vấn danh sách phân công của 1 Trọng tài cụ thể, có lọc theo trạng thái trận đấu
    @Query("SELECT mr FROM MatchReferee mr " +
            "JOIN FETCH mr.match m " +
            "JOIN FETCH m.tournament t " +
            "JOIN FETCH m.homeClub hc " +
            "JOIN FETCH m.awayClub ac " +
            "LEFT JOIN FETCH m.court c " +
            "LEFT JOIN FETCH c.venue v " +
            "WHERE mr.referee.id = :refereeId " +
            "AND m.status IN :matchStatuses " +
            "ORDER BY m.scheduledTime ASC")
    List<MatchReferee> findAssignedMatchesByRefereeAndStatus(
            @Param("refereeId") Long refereeId,
            @Param("matchStatuses") List<MatchStatus> matchStatuses);

    boolean existsByMatchIdAndRefereeId(Long matchId, Long refereeId);
    Optional<MatchReferee> findByMatchIdAndRefereeId(Long matchId, Long refereeId);
    // =============================================================



    // Xóa một trọng tài khỏi trận đấu
    void deleteByMatchIdAndRefereeId(Long matchId, Long refereeId);

}
