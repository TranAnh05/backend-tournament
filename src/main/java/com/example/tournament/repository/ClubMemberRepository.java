//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Club;
import com.example.tournament.entity.ClubMember;
import com.example.tournament.enums.ClubRole;
import com.example.tournament.enums.JoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {

    // Lấy tất cả thành viên của CLB theo trạng thái
    List<ClubMember> findByClubAndJoinStatus(Club club, JoinStatus joinStatus);

    // Lấy tất cả thành viên APPROVED của CLB
    @Query("SELECT cm FROM ClubMember cm JOIN FETCH cm.athlete a JOIN FETCH a.user WHERE cm.club = :club AND cm.joinStatus = 'APPROVED'")
    List<ClubMember> findApprovedMembersWithDetails(@Param("club") Club club);

    // Kiểm tra VĐV đã là thành viên APPROVED của CLB chưa
    boolean existsByClubAndAthleteIdAndJoinStatus(Club club, Long athleteId, JoinStatus joinStatus);

    // Lấy thành viên cụ thể trong CLB
    Optional<ClubMember> findByIdAndClub(Long id, Club club);

    // Tìm thành viên theo vai trò trong CLB (để reset captain/head_coach khi phân công lại)
    Optional<ClubMember> findByClubAndClubRoleAndJoinStatus(Club club, ClubRole role, JoinStatus joinStatus);

    // Kiểm tra số áo đã tồn tại trong CLB chưa
    @Query("SELECT COUNT(cm) > 0 FROM ClubMember cm WHERE cm.club = :club AND cm.joinStatus = 'APPROVED' AND cm.athlete.preferredNumber = :number AND cm.athlete.id != :excludeAthleteId")
    boolean existsJerseyNumberInClub(@Param("club") Club club, @Param("number") Integer number, @Param("excludeAthleteId") Long excludeAthleteId);
}