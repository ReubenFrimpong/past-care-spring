package com.reuben.pastcare_spring.jobs;

import com.reuben.pastcare_spring.models.ChurchSmsCredit;
import com.reuben.pastcare_spring.services.ChurchSmsCreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job to check for churches with low SMS balance
 * and send alerts to administrators
 */
@Component
@Slf4j
public class SmsLowBalanceAlertJob {

    private final ChurchSmsCreditService churchSmsCreditService;

    public SmsLowBalanceAlertJob(ChurchSmsCreditService churchSmsCreditService) {
        this.churchSmsCreditService = churchSmsCreditService;
    }

    /**
     * Check for low balance every day at 9 AM
     * Cron: 0 0 9 * * * (second, minute, hour, day, month, day-of-week)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkLowBalance() {
        log.info("Running low balance alert job...");

        try {
            List<ChurchSmsCredit> churchesNeedingAlert =
                churchSmsCreditService.getChurchesNeedingLowBalanceAlert();

            if (churchesNeedingAlert.isEmpty()) {
                log.info("No churches with low balance requiring alerts");
                return;
            }

            log.info("Found {} churches with low balance", churchesNeedingAlert.size());

            for (ChurchSmsCredit credit : churchesNeedingAlert) {
                try {
                    sendLowBalanceAlert(credit);
                    churchSmsCreditService.markLowBalanceAlertSent(credit.getChurch().getId());
                    log.info("Low balance alert sent for church: {}", credit.getChurch().getName());
                } catch (Exception e) {
                    log.error("Failed to send low balance alert for church {}: {}",
                        credit.getChurch().getName(), e.getMessage(), e);
                }
            }

            log.info("Low balance alert job completed successfully");

        } catch (Exception e) {
            log.error("Error in low balance alert job: {}", e.getMessage(), e);
        }
    }

    /**
     * Send low balance alert
     * TODO: Integrate with email service when available
     */
    private void sendLowBalanceAlert(ChurchSmsCredit credit) {
        // For now, just log the alert
        // In future, this will:
        // 1. Send email to church administrators
        // 2. Create in-app notification
        // 3. Optionally send SMS to primary contact

        log.warn("LOW BALANCE ALERT - Church: {}, Current Balance: {}, Threshold: {}",
            credit.getChurch().getName(),
            credit.getBalance(),
            credit.getLowBalanceThreshold());

        // TODO: Implement email notification
        // emailService.sendLowBalanceAlert(credit);

        // TODO: Implement in-app notification
        // notificationService.createNotification(
        //     credit.getChurch(),
        //     "Low SMS Credit Balance",
        //     "Your SMS balance is running low. Current: " + credit.getBalance()
        // );
    }

    /**
     * Manual trigger for testing (called via API endpoint)
     */
    public void runManually() {
        log.info("Manually triggered low balance alert job");
        checkLowBalance();
    }
}
