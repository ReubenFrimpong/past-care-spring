package com.reuben.pastcare_spring.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reuben.pastcare_spring.models.Church;

public interface ChurchRepository extends JpaRepository<Church, Long> {

}
