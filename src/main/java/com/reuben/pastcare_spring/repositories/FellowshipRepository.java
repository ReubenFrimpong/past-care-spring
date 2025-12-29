package com.reuben.pastcare_spring.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;

public interface FellowshipRepository extends JpaRepository<Fellowship, Long> {
    java.util.List<Fellowship> findByChurch_Id(Long churchId);

    Long countByChurch(Church church);
}
