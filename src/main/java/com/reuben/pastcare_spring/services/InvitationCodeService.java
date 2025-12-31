package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.InvitationCode;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.InvitationCodeRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Service for managing invitation codes.
 */
@Service
@RequiredArgsConstructor
public class InvitationCodeService {

    private final InvitationCodeRepository invitationCodeRepository;
    private final ChurchRepository churchRepository;
    private final UserRepository userRepository;

    private static final String CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int DEFAULT_CODE_LENGTH = 8;

    /**
     * Generate a new invitation code for a church.
     */
    @Transactional
    public InvitationCode createInvitationCode(Long churchId, Long userId,
                                               String description, Integer maxUses,
                                               LocalDateTime expiresAt, Role defaultRole) {
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate unique code
        String code = generateUniqueCode();

        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setChurch(church);
        invitationCode.setCreatedBy(user);
        invitationCode.setCode(code);
        invitationCode.setDescription(description);
        invitationCode.setMaxUses(maxUses);
        invitationCode.setExpiresAt(expiresAt);
        invitationCode.setDefaultRole(defaultRole != null ? defaultRole : Role.MEMBER);

        return invitationCodeRepository.save(invitationCode);
    }

    /**
     * Get all invitation codes for a church.
     */
    @Transactional(readOnly = true)
    public List<InvitationCode> getChurchInvitationCodes(Long churchId) {
        return invitationCodeRepository.findByChurchId(churchId);
    }

    /**
     * Get active invitation codes for a church.
     */
    @Transactional(readOnly = true)
    public List<InvitationCode> getActiveInvitationCodes(Long churchId) {
        return invitationCodeRepository.findByChurchIdAndIsActiveTrue(churchId);
    }

    /**
     * Validate and retrieve invitation code.
     */
    @Transactional(readOnly = true)
    public Optional<InvitationCode> validateInvitationCode(String code) {
        Optional<InvitationCode> invitationCode = invitationCodeRepository.findByCode(code);

        if (invitationCode.isPresent() && invitationCode.get().isValid()) {
            return invitationCode;
        }

        return Optional.empty();
    }

    /**
     * Use an invitation code (increment usage count).
     */
    @Transactional
    public void useInvitationCode(String code) {
        InvitationCode invitationCode = invitationCodeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Invitation code not found"));

        if (!invitationCode.isValid()) {
            throw new RuntimeException("Invitation code is not valid");
        }

        invitationCode.incrementUsage();
        invitationCodeRepository.save(invitationCode);
    }

    /**
     * Deactivate an invitation code.
     */
    @Transactional
    public void deactivateInvitationCode(Long id, Long churchId) {
        InvitationCode invitationCode = invitationCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation code not found"));

        if (!invitationCode.getChurch().getId().equals(churchId)) {
            throw new RuntimeException("Unauthorized access to invitation code");
        }

        invitationCode.setIsActive(false);
        invitationCodeRepository.save(invitationCode);
    }

    /**
     * Delete an invitation code.
     */
    @Transactional
    public void deleteInvitationCode(Long id, Long churchId) {
        InvitationCode invitationCode = invitationCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation code not found"));

        if (!invitationCode.getChurch().getId().equals(churchId)) {
            throw new RuntimeException("Unauthorized access to invitation code");
        }

        invitationCodeRepository.delete(invitationCode);
    }

    /**
     * Cleanup expired codes (scheduled task).
     */
    @Transactional
    public void cleanupExpiredCodes() {
        List<InvitationCode> expiredCodes = invitationCodeRepository.findExpiredActiveCodes(LocalDateTime.now());
        expiredCodes.forEach(code -> code.setIsActive(false));
        invitationCodeRepository.saveAll(expiredCodes);

        List<InvitationCode> fullyUsedCodes = invitationCodeRepository.findFullyUsedCodes();
        fullyUsedCodes.forEach(code -> code.setIsActive(false));
        invitationCodeRepository.saveAll(fullyUsedCodes);
    }

    /**
     * Generate a unique random code.
     */
    private String generateUniqueCode() {
        Random random = new Random();
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            StringBuilder sb = new StringBuilder(DEFAULT_CODE_LENGTH);
            for (int i = 0; i < DEFAULT_CODE_LENGTH; i++) {
                int index = random.nextInt(CODE_CHARACTERS.length());
                sb.append(CODE_CHARACTERS.charAt(index));
            }
            code = sb.toString();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate unique invitation code");
            }
        } while (invitationCodeRepository.findByCode(code).isPresent());

        return code;
    }

    /**
     * Get invitation code by ID (with church validation).
     */
    @Transactional(readOnly = true)
    public InvitationCode getInvitationCodeById(Long id, Long churchId) {
        InvitationCode code = invitationCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation code not found"));

        if (!code.getChurch().getId().equals(churchId)) {
            throw new RuntimeException("Unauthorized access to invitation code");
        }

        return code;
    }
}
