package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.TransactionStatus;
import com.reuben.pastcare_spring.models.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SmsTransactionResponse {

    private Long id;
    private Long userId;
    private String userName;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String referenceId;
    private String paymentReference;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
