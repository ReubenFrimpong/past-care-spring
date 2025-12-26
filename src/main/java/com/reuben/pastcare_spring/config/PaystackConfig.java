package com.reuben.pastcare_spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Paystack payment gateway
 */
@Configuration
@ConfigurationProperties(prefix = "paystack")
@Data
public class PaystackConfig {

    private String secretKey;
    private String publicKey;
    private String baseUrl = "https://api.paystack.co";
    private String callbackUrl;
    private Boolean testMode = true;

    // Webhook configuration
    private String webhookSecret;

    // Retry configuration
    private Integer maxRetryAttempts = 3;
    private Long initialRetryDelayMinutes = 60L; // 1 hour
    private Long maxRetryDelayHours = 48L; // 48 hours

    // Transaction limits
    private Integer transactionTimeoutSeconds = 900; // 15 minutes
}
