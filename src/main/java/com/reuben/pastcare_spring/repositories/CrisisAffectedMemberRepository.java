package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Crisis;
import com.reuben.pastcare_spring.models.CrisisAffectedMember;
import com.reuben.pastcare_spring.models.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrisisAffectedMemberRepository extends JpaRepository<CrisisAffectedMember, Long> {

    // Find by crisis
    List<CrisisAffectedMember> findByCrisis(Crisis crisis);

    // Find by member
    List<CrisisAffectedMember> findByMember(Member member);

    // Find by crisis and member
    Optional<CrisisAffectedMember> findByCrisisAndMember(Crisis crisis, Member member);

    // Delete by crisis and member
    void deleteByCrisisAndMember(Crisis crisis, Member member);

    // Count by crisis
    Long countByCrisis(Crisis crisis);
}
