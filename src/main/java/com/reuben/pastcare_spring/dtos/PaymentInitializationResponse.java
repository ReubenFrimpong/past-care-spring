package com.reuben.pastcare_spring.dtos;

import lombok.Data;

/**
 * Response DTO for payment initialization
 */
@Data
public class PaymentInitializationResponse {

    private Boolean status;
    private String message;
    private String authorizationUrl;
    private String accessCode;
    private String reference;
}
