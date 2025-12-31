package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.PasswordResetToken;
import com.reuben.pastcare_spring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findByUser(User user);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    void deleteByUser(User user);
}
