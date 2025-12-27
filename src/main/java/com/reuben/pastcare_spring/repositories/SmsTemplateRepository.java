package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.SmsTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {

    Page<SmsTemplate> findByChurchIdAndIsActive(Long churchId, Boolean isActive, Pageable pageable);

    List<SmsTemplate> findByChurchIdAndIsActiveOrderByUsageCountDesc(Long churchId, Boolean isActive);

    Optional<SmsTemplate> findByChurchIdAndId(Long churchId, Long id);

    List<SmsTemplate> findByChurchIdAndCategoryAndIsActive(Long churchId, String category, Boolean isActive);

    List<SmsTemplate> findByChurchIdAndIsDefault(Long churchId, Boolean isDefault);
}
