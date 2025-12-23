package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Skill;
import com.reuben.pastcare_spring.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByChurchIdAndIsActive(Long churchId, Boolean isActive);

    List<Skill> findByChurchId(Long churchId);

    List<Skill> findByChurchIdAndCategory(Long churchId, SkillCategory category);

    Optional<Skill> findByNameAndChurchId(String name, Long churchId);

    @Query("SELECT s FROM Skill s WHERE s.churchId = :churchId " +
           "AND s.isActive = true " +
           "AND LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Skill> searchSkills(@Param("churchId") Long churchId,
                            @Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(s) FROM Skill s WHERE s.churchId = :churchId AND s.isActive = true")
    Long countActiveSkills(@Param("churchId") Long churchId);
}
