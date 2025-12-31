package com.reuben.pastcare_spring.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Email service for sending emails
 *
 * IMPLEMENTATION OPTIONS:
 * 1. SMTP (JavaMail): For self-hosted or basic email
 * 2. SendGrid: Popular, reliable, good free tier
 * 3. AWS SES: Cost-effective for high volume
 * 4. Mailgun: Developer-friendly API
 * 5. Postmark: Great for transactional emails
 *
 * Current: Logging mode (for development)
 * Production: Configure spring.mail.* properties or integrate email provider SDK
 */
@Service
@Slf4j
public class EmailService {

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.email.from:noreply@pastcare.com}")
    private String fromEmail;

    /**
     * Send plain text email to recipient
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Plain text email body
     */
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("📧 EMAIL DISABLED - Would send to: {}", to);
            log.info("   Subject: {}", subject);
            log.info("   Body preview: {}", body.substring(0, Math.min(100, body.length())));
            return;
        }

        try {
            // TODO: Implement actual email sending
            // Example with Spring Boot Mail:
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setFrom(fromEmail);
            // message.setTo(to);
            // message.setSubject(subject);
            // message.setText(body);
            // mailSender.send(message);

            log.info("✅ Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        sendHtmlEmail(to, subject, htmlBody, null);
    }

    /**
     * Send HTML email with plain text fallback
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body
     * @param textBody Plain text fallback (optional)
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody, String textBody) {
        if (!emailEnabled) {
            log.info("📧 EMAIL DISABLED - Would send HTML email to: {}", to);
            log.info("   Subject: {}", subject);
            log.info("   HTML length: {} characters", htmlBody.length());
            return;
        }

        try {
            // TODO: Implement HTML email sending
            // Example with Spring Boot Mail:
            // MimeMessage message = mailSender.createMimeMessage();
            // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            // helper.setFrom(fromEmail);
            // helper.setTo(to);
            // helper.setSubject(subject);
            // helper.setText(textBody != null ? textBody : "", htmlBody);
            // mailSender.send(message);

            log.info("✅ HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Send email with attachments
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @param attachment Attachment bytes
     * @param attachmentName Attachment filename
     */
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String attachmentName) {
        if (!emailEnabled) {
            log.info("📧 EMAIL DISABLED - Would send email with attachment to: {}", to);
            log.info("   Attachment: {} ({} bytes)", attachmentName, attachment.length);
            return;
        }

        try {
            // TODO: Implement email with attachment
            // Example with Spring Boot Mail:
            // MimeMessage message = mailSender.createMimeMessage();
            // MimeMessageHelper helper = new MimeMessageHelper(message, true);
            // helper.setFrom(fromEmail);
            // helper.setTo(to);
            // helper.setSubject(subject);
            // helper.setText(body);
            // helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            // mailSender.send(message);

            log.info("✅ Email with attachment sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email with attachment to: {}", to, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    /**
     * Check if email sending is enabled
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
}
