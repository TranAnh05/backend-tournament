package com.example.tournament.repository;

import com.example.tournament.entity.Venue;
import com.example.tournament.enums.CommonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//clb
package com.example.tournament.repository;

import com.example.tournament.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    @EntityGraph(attributePaths = {"courts", "courts.supportedSports"})
    @Query("SELECT DISTINCT v FROM Venue v WHERE " +
            "(:search IS NULL OR :search = '' OR LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR v.status = :status)")
    Page<Venue> searchVenues(@Param("search") String search,
                             @Param("status") CommonStatus status,
                             Pageable pageable);

    boolean existsByName(String name);
}
