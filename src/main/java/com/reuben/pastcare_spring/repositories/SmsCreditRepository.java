package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.SmsCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsCreditRepository extends JpaRepository<SmsCredit, Long> {

    Optional<SmsCredit> findByUserIdAndChurchId(Long userId, Long churchId);

    boolean existsByUserIdAndChurchId(Long userId, Long churchId);
}
