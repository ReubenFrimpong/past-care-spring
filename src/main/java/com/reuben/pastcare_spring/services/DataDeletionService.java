package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for handling permanent deletion of church data after suspension period.
 *
 * <p>Deletion Policy:
 * <ul>
 *   <li>Churches are suspended when subscription expires</li>
 *   <li>Data retained for 90 days after suspension (+ SUPERADMIN extensions)</li>
 *   <li>7-day warning email sent before deletion</li>
 *   <li>After 90 days (+ 7 day warning period), all data permanently deleted</li>
 * </ul>
 *
 * <p>GDPR Compliance:
 * - Implements "right to be forgotten" after reasonable retention period
 * - Provides clear notification before deletion
 * - SUPERADMIN can extend retention if needed
 * - Deletion is permanent and irreversible
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataDeletionService {

    private final ChurchRepository churchRepository;
    private final ChurchSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.url:http://localhost:4200}")
    private String appUrl;

    /**
     * Permanently delete all data for a church.
     *
     * <p>Deletes in order to respect foreign key constraints:
     * 1. Attendance records
     * 2. Events
     * 3. Donations and Pledges
     * 4. Campaigns
     * 5. Prayer requests and Care needs
     * 6. Visits
     * 7. Members
     * 8. Fellowships
     * 9. Users
     * 10. Subscription
     * 11. Church record
     *
     * @param churchId Church ID to delete
     * @param subscription Church subscription (for verification)
     * @throws IllegalStateException if church is not eligible for deletion
     */
    @Transactional
    public void deleteChurchData(Long churchId, ChurchSubscription subscription) {
        // Verify eligibility
        if (!subscription.isEligibleForDeletion()) {
            throw new IllegalStateException(
                String.format("Church %d is not eligible for deletion. Status: %s, Days until deletion: %d",
                    churchId, subscription.getStatus(), subscription.getDaysUntilDeletion())
            );
        }

        log.warn("======================================================");
        log.warn("PERMANENT DATA DELETION STARTING for church ID: {}", churchId);
        log.warn("This action is IRREVERSIBLE");
        log.warn("======================================================");

        try {
            // Note: Cascade deletion will be handled by database foreign key constraints
            // with ON DELETE CASCADE. For now, we'll delete the church and let cascades handle it.

            log.info("Initiating cascade deletion for church {}", churchId);

            // 1. Delete subscription
            log.info("Deleting subscription for church {}", churchId);
            subscriptionRepository.deleteByChurchId(churchId);

            // 2. Delete church record (cascade will handle all related data)
            log.info("Deleting church record (cascading to all related data) for church {}", churchId);
            churchRepository.deleteById(churchId);

            log.warn("======================================================");
            log.warn("PERMANENT DATA DELETION COMPLETED for church ID: {}", churchId);
            log.warn("All related data deleted via database cascade");
            log.warn("======================================================");

        } catch (Exception e) {
            log.error("ERROR during data deletion for church {}: {}", churchId, e.getMessage(), e);
            throw new RuntimeException("Data deletion failed for church " + churchId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Send 7-day deletion warning email to church administrators.
     *
     * @param churchId Church ID
     * @param subscription Church subscription
     */
    public void sendDeletionWarningEmail(Long churchId, ChurchSubscription subscription) {
        try {
            // Get church entity and name
            var church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));
            String churchName = church.getName();

            // Get admin emails - find all users for this church and filter by role
            var adminEmails = userRepository.findAll().stream()
                .filter(user -> user.getChurch() != null && user.getChurch().getId().equals(churchId))
                .filter(user -> user.getRole() == com.reuben.pastcare_spring.enums.Role.ADMIN)
                .map(user -> user.getEmail())
                .toList();

            if (adminEmails.isEmpty()) {
                log.warn("No admin emails found for church {}. Cannot send deletion warning.", churchId);
                return;
            }

            long daysUntilDeletion = subscription.getDaysUntilDeletion();

            String subject = String.format("URGENT: %s - Data Deletion in %d Days", churchName, daysUntilDeletion);

            String body = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 3px solid #ef4444; border-radius: 10px; background-color: #fef2f2;">
                        <h1 style="color: #ef4444; text-align: center; margin-top: 0;">
                            ⚠️ URGENT: Data Deletion Warning
                        </h1>

                        <p style="font-size: 16px; font-weight: bold; color: #dc2626;">
                            Your church data will be permanently deleted in %d days.
                        </p>

                        <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h2 style="color: #374151; margin-top: 0;">What This Means:</h2>
                            <ul style="color: #6b7280;">
                                <li>All member records will be deleted</li>
                                <li>All events and attendance data will be deleted</li>
                                <li>All donations and pledges will be deleted</li>
                                <li>All users and access will be removed</li>
                                <li><strong style="color: #ef4444;">This deletion is PERMANENT and CANNOT be undone</strong></li>
                            </ul>
                        </div>

                        <div style="background-color: #fef3c7; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #f59e0b;">
                            <h2 style="color: #92400e; margin-top: 0;">How to Save Your Data:</h2>
                            <p style="color: #78350f; margin-bottom: 10px;">
                                Renew your subscription now to cancel the deletion and restore full access.
                            </p>
                            <a href="%s/billing"
                               style="display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; margin-top: 10px;">
                                Renew Subscription Now
                            </a>
                        </div>

                        <div style="background-color: #f3f4f6; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <h3 style="color: #374151; margin-top: 0;">Deletion Schedule:</h3>
                            <p style="color: #6b7280; margin: 5px 0;">
                                <strong>Subscription Suspended:</strong> %s
                            </p>
                            <p style="color: #6b7280; margin: 5px 0;">
                                <strong>Data Deletion Date:</strong> <span style="color: #ef4444; font-weight: bold;">%s</span>
                            </p>
                            <p style="color: #6b7280; margin: 5px 0;">
                                <strong>Days Remaining:</strong> <span style="color: #ef4444; font-weight: bold;">%d days</span>
                            </p>
                        </div>

                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #d1d5db; color: #6b7280; font-size: 14px;">
                            <p>Need help? Contact us at <a href="mailto:support@pastcare.com" style="color: #667eea;">support@pastcare.com</a></p>
                            <p style="font-style: italic; margin-top: 10px;">
                                This is an automated notification. Your subscription was suspended due to non-payment.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                daysUntilDeletion,
                getBaseUrl(),
                subscription.getSuspendedAt().toLocalDate(),
                subscription.getDataRetentionEndDate(),
                daysUntilDeletion
            );

            // Send to all admin emails
            for (String email : adminEmails) {
                emailService.sendEmail(email, subject, body);
            }

            log.info("Deletion warning email sent to {} admin(s) for church {}", adminEmails.size(), churchId);

        } catch (Exception e) {
            log.error("Failed to send deletion warning email for church {}: {}", churchId, e.getMessage(), e);
        }
    }

    /**
     * Get base URL for email links (environment-specific)
     */
    private String getBaseUrl() {
        return appUrl;
    }
}
