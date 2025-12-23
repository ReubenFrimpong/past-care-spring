package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.MemberSkill;
import com.reuben.pastcare_spring.enums.ProficiencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberSkillRepository extends JpaRepository<MemberSkill, Long> {

    List<MemberSkill> findByMemberId(Long memberId);

    List<MemberSkill> findBySkillId(Long skillId);

    Optional<MemberSkill> findByMemberIdAndSkillId(Long memberId, Long skillId);

    @Query("SELECT ms FROM MemberSkill ms WHERE ms.churchId = :churchId " +
           "AND ms.skill.id = :skillId AND ms.willingToServe = true")
    List<MemberSkill> findMembersWillingToServeBySkill(@Param("churchId") Long churchId,
                                                        @Param("skillId") Long skillId);

    @Query("SELECT ms FROM MemberSkill ms WHERE ms.churchId = :churchId " +
           "AND ms.skill.id = :skillId " +
           "AND ms.proficiencyLevel IN :proficiencyLevels " +
           "AND ms.willingToServe = true")
    List<MemberSkill> findMembersBySkillAndProficiency(@Param("churchId") Long churchId,
                                                        @Param("skillId") Long skillId,
                                                        @Param("proficiencyLevels") List<ProficiencyLevel> proficiencyLevels);

    @Query("SELECT ms FROM MemberSkill ms WHERE ms.member.id = :memberId " +
           "AND ms.currentlyServing = true")
    List<MemberSkill> findCurrentlyServingSkills(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(ms) FROM MemberSkill ms WHERE ms.member.id = :memberId")
    Long countMemberSkills(@Param("memberId") Long memberId);

    void deleteByMemberIdAndSkillId(Long memberId, Long skillId);
}
