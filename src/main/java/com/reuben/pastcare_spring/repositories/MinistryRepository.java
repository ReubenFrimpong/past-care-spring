package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Ministry;
import com.reuben.pastcare_spring.enums.MinistryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MinistryRepository extends JpaRepository<Ministry, Long> {

    List<Ministry> findByChurchId(Long churchId);

    List<Ministry> findByChurchIdAndStatus(Long churchId, MinistryStatus status);

    Optional<Ministry> findByNameAndChurchId(String name, Long churchId);

    @Query("SELECT m FROM Ministry m WHERE m.churchId = :churchId " +
           "AND LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Ministry> searchMinistries(@Param("churchId") Long churchId,
                                    @Param("searchTerm") String searchTerm);

    @Query("SELECT m FROM Ministry m WHERE m.leader.id = :leaderId")
    List<Ministry> findByLeaderId(@Param("leaderId") Long leaderId);

    @Query("SELECT m FROM Ministry m " +
           "JOIN m.members mem " +
           "WHERE mem.id = :memberId")
    List<Ministry> findMinistriesByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(m) FROM Ministry m WHERE m.churchId = :churchId AND m.status = 'ACTIVE'")
    Long countActiveMinistries(@Param("churchId") Long churchId);
}
