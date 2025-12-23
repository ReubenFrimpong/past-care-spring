package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.PortalLoginRequest;
import com.reuben.pastcare_spring.dtos.PortalRegistrationRequest;
import com.reuben.pastcare_spring.dtos.PortalUserResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.PortalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PortalUserService {

    private final PortalUserRepository portalUserRepository;
    private final MemberRepository memberRepository;
    private final ChurchRepository churchRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int VERIFICATION_TOKEN_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 2;

    /**
     * Register a new portal user (member self-registration)
     */
    public PortalUserResponse registerPortalUser(Long churchId, PortalRegistrationRequest request) {
        // Check if email already exists for this church
        portalUserRepository.findByEmailAndChurchId(request.getEmail(), churchId)
            .ifPresent(u -> {
                throw new IllegalArgumentException("Email already registered for this church");
            });

        // Check if church exists
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with ID: " + churchId));

        // Create member record
        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setPhoneNumber(request.getPhoneNumber());

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
            member.setDob(LocalDate.parse(request.getDateOfBirth()));
        }

        // TODO: Create location entity for address if provided
        member.setStatus(MemberStatus.VISITOR); // Start as visitor
        member.setChurch(church);
        member.setIsVerified(false); // Not verified until approved

        Member savedMember = memberRepository.save(member);
        log.info("Created member record for portal registration: {} (ID: {})", request.getEmail(), savedMember.getId());

        // Create portal user
        PortalUser portalUser = new PortalUser();
        portalUser.setEmail(request.getEmail());
        portalUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        portalUser.setMember(savedMember);
        portalUser.setStatus(PortalUserStatus.PENDING_VERIFICATION);
        portalUser.setVerificationToken(generateToken());
        portalUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS));
        portalUser.setIsActive(false);
        portalUser.setChurchId(churchId);

        PortalUser savedPortalUser = portalUserRepository.save(portalUser);
        log.info("Created portal user: {} (ID: {})", savedPortalUser.getEmail(), savedPortalUser.getId());

        // Send verification email
        sendVerificationEmail(savedPortalUser, church.getName());

        return mapToResponse(savedPortalUser);
    }

    /**
     * Verify email with token
     */
    public PortalUserResponse verifyEmail(String token) {
        PortalUser portalUser = portalUserRepository.findByVerificationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        // Check if token is expired
        if (portalUser.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        // Check if already verified
        if (portalUser.getEmailVerifiedAt() != null) {
            throw new IllegalArgumentException("Email already verified");
        }

        // Update status
        portalUser.setEmailVerifiedAt(LocalDateTime.now());
        portalUser.setStatus(PortalUserStatus.PENDING_APPROVAL);
        portalUser.setVerificationToken(null);
        portalUser.setVerificationTokenExpiry(null);

        PortalUser updated = portalUserRepository.save(portalUser);
        log.info("Email verified for portal user: {} (ID: {})", updated.getEmail(), updated.getId());

        // Notify admins about pending approval
        notifyAdminsOfPendingApproval(updated);

        return mapToResponse(updated);
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email, Long churchId) {
        PortalUser portalUser = portalUserRepository.findByEmailAndChurchId(email, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Portal user not found"));

        if (portalUser.getEmailVerifiedAt() != null) {
            throw new IllegalArgumentException("Email already verified");
        }

        // Generate new token
        portalUser.setVerificationToken(generateToken());
        portalUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS));
        portalUserRepository.save(portalUser);

        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found"));

        sendVerificationEmail(portalUser, church.getName());
        log.info("Resent verification email to: {}", email);
    }

    /**
     * Approve portal user (admin action)
     */
    public PortalUserResponse approvePortalUser(Long churchId, Long portalUserId, Long approvedByUserId) {
        PortalUser portalUser = portalUserRepository.findById(portalUserId)
            .orElseThrow(() -> new IllegalArgumentException("Portal user not found"));

        if (!portalUser.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to portal user");
        }

        if (portalUser.getStatus() != PortalUserStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Portal user is not pending approval");
        }

        // Update portal user
        portalUser.setStatus(PortalUserStatus.APPROVED);
        portalUser.setIsActive(true);
        portalUser.setApprovedAt(LocalDateTime.now());
        // Note: approvedBy should be set by passing User object, simplified here

        // Activate member
        Member member = portalUser.getMember();
        member.setIsVerified(true);
        member.setStatus(MemberStatus.MEMBER);
        memberRepository.save(member);

        PortalUser updated = portalUserRepository.save(portalUser);
        log.info("Approved portal user: {} (ID: {})", updated.getEmail(), updated.getId());

        // Send approval email
        sendApprovalEmail(updated);

        return mapToResponse(updated);
    }

    /**
     * Reject portal user (admin action)
     */
    public PortalUserResponse rejectPortalUser(Long churchId, Long portalUserId, String reason) {
        PortalUser portalUser = portalUserRepository.findById(portalUserId)
            .orElseThrow(() -> new IllegalArgumentException("Portal user not found"));

        if (!portalUser.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to portal user");
        }

        if (portalUser.getStatus() != PortalUserStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Portal user is not pending approval");
        }

        portalUser.setStatus(PortalUserStatus.REJECTED);
        portalUser.setRejectionReason(reason);

        PortalUser updated = portalUserRepository.save(portalUser);
        log.info("Rejected portal user: {} (ID: {})", updated.getEmail(), updated.getId());

        // Send rejection email
        sendRejectionEmail(updated);

        return mapToResponse(updated);
    }

    /**
     * Get all portal users by status
     */
    @Transactional(readOnly = true)
    public List<PortalUserResponse> getPortalUsersByStatus(Long churchId, PortalUserStatus status) {
        return portalUserRepository.findByChurchIdAndStatus(churchId, status)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get portal user by ID
     */
    @Transactional(readOnly = true)
    public PortalUserResponse getPortalUserById(Long churchId, Long portalUserId) {
        PortalUser portalUser = portalUserRepository.findById(portalUserId)
            .orElseThrow(() -> new IllegalArgumentException("Portal user not found"));

        if (!portalUser.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to portal user");
        }

        return mapToResponse(portalUser);
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email, Long churchId) {
        PortalUser portalUser = portalUserRepository.findByEmailAndChurchId(email, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Portal user not found"));

        // Generate reset token
        String resetToken = generateToken();
        portalUser.setPasswordResetToken(resetToken);
        portalUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(PASSWORD_RESET_TOKEN_EXPIRY_HOURS));
        portalUserRepository.save(portalUser);

        // Send reset email
        sendPasswordResetEmail(portalUser);
        log.info("Password reset requested for: {}", email);
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        PortalUser portalUser = portalUserRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        // Check if token is expired
        if (portalUser.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        // Update password
        portalUser.setPasswordHash(passwordEncoder.encode(newPassword));
        portalUser.setPasswordResetToken(null);
        portalUser.setPasswordResetTokenExpiry(null);
        portalUserRepository.save(portalUser);

        log.info("Password reset for portal user: {}", portalUser.getEmail());
    }

    // Helper methods

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private void sendVerificationEmail(PortalUser portalUser, String churchName) {
        String subject = "Verify Your Email - " + churchName + " Portal";
        String verificationLink = "https://portal.example.com/verify?token=" + portalUser.getVerificationToken();
        String body = String.format(
            "Hello %s %s,\n\n" +
            "Thank you for registering with %s portal.\n\n" +
            "Please click the link below to verify your email address:\n%s\n\n" +
            "This link will expire in %d hours.\n\n" +
            "If you didn't register for this account, please ignore this email.\n\n" +
            "Best regards,\n%s",
            portalUser.getMember().getFirstName(),
            portalUser.getMember().getLastName(),
            churchName,
            verificationLink,
            VERIFICATION_TOKEN_EXPIRY_HOURS,
            churchName
        );

        emailService.sendEmail(portalUser.getEmail(), subject, body);
    }

    private void sendApprovalEmail(PortalUser portalUser) {
        // Implementation for sending approval email
        log.info("Sending approval email to: {}", portalUser.getEmail());
    }

    private void sendRejectionEmail(PortalUser portalUser) {
        // Implementation for sending rejection email
        log.info("Sending rejection email to: {}", portalUser.getEmail());
    }

    private void sendPasswordResetEmail(PortalUser portalUser) {
        // Implementation for sending password reset email
        log.info("Sending password reset email to: {}", portalUser.getEmail());
    }

    private void notifyAdminsOfPendingApproval(PortalUser portalUser) {
        // Implementation for notifying admins
        log.info("Notifying admins of pending approval for: {}", portalUser.getEmail());
    }

    private PortalUserResponse mapToResponse(PortalUser portalUser) {
        PortalUserResponse response = new PortalUserResponse();
        response.setId(portalUser.getId());
        response.setEmail(portalUser.getEmail());
        response.setStatus(portalUser.getStatus());
        response.setEmailVerifiedAt(portalUser.getEmailVerifiedAt());
        response.setApprovedAt(portalUser.getApprovedAt());
        response.setRejectionReason(portalUser.getRejectionReason());
        response.setLastLoginAt(portalUser.getLastLoginAt());
        response.setIsActive(portalUser.getIsActive());
        if (portalUser.getCreatedAt() != null) {
            response.setCreatedAt(LocalDateTime.ofInstant(portalUser.getCreatedAt(), java.time.ZoneId.systemDefault()));
        }

        if (portalUser.getMember() != null) {
            response.setMemberId(portalUser.getMember().getId());
            response.setMemberFirstName(portalUser.getMember().getFirstName());
            response.setMemberLastName(portalUser.getMember().getLastName());
        }

        if (portalUser.getApprovedBy() != null) {
            response.setApprovedByName(portalUser.getApprovedBy().getEmail());
        }

        return response;
    }
}
