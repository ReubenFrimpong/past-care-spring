package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.FellowshipMultiplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FellowshipMultiplicationRepository extends JpaRepository<FellowshipMultiplication, Long> {

    /**
     * Find all multiplications for a parent fellowship
     */
    List<FellowshipMultiplication> findByParentFellowship(Fellowship parentFellowship);

    /**
     * Find all multiplications for a child fellowship
     */
    List<FellowshipMultiplication> findByChildFellowship(Fellowship childFellowship);

    /**
     * Find all multiplications ordered by date descending
     */
    List<FellowshipMultiplication> findAllByOrderByMultiplicationDateDesc();
}
