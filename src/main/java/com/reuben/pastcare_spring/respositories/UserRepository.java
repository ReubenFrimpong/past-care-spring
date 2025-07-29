package com.reuben.pastcare_spring.respositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reuben.pastcare_spring.models.User;

public interface UserRepository extends JpaRepository<User, Integer> {

}
