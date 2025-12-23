package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PortalUser entity for member self-service portal
 * Linked to Member entity for authenticated member access
 */
@Entity
@Table(name = "portal_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email", "church_id"}),
    @UniqueConstraint(columnNames = {"member_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PortalUser extends TenantBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PortalUserStatus status = PortalUserStatus.PENDING_VERIFICATION;

    @Column(length = 100)
    private String verificationToken;

    @Column
    private LocalDateTime verificationTokenExpiry;

    @Column
    private LocalDateTime emailVerifiedAt;

    @Column
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(length = 500)
    private String rejectionReason;

    @Column
    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private Boolean isActive = false;

    @Column(length = 100)
    private String passwordResetToken;

    @Column
    private LocalDateTime passwordResetTokenExpiry;
}
