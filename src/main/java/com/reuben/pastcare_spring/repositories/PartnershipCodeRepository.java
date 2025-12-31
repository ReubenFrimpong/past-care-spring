package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.PartnershipCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnershipCodeRepository extends JpaRepository<PartnershipCode, Long> {
    Optional<PartnershipCode> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
