package com.reuben.pastcare_spring.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.models.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

}
